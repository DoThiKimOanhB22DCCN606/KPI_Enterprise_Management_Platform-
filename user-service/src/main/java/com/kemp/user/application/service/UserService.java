package com.kemp.user.application.service;

import com.kemp.user.application.dto.CreateUserRequest;
import com.kemp.user.application.dto.InviteUserRequest;
import com.kemp.user.application.dto.UpdateUserRequest;
import com.kemp.user.domain.model.Role;
import com.kemp.user.domain.model.User;
import com.kemp.user.domain.repository.RoleRepository;
import com.kemp.user.domain.repository.UserRepository;
import com.kemp.user.infrastructure.config.TenantContext;
import com.kemp.user.infrastructure.messaging.UserEventPublisher;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserEventPublisher eventPublisher;
    private final StringRedisTemplate redisTemplate;
    private final JdbcTemplate jdbcTemplate;
    private final JavaMailSender mailSender;

    @Transactional
    public User createUser(CreateUserRequest request) {
        UUID tenantId = getTenantId();
        
        if (userRepository.findByEmailAndTenantId(request.getEmail(), tenantId).isPresent()) {
            throw new RuntimeException("Email already exists in this tenant");
        }

        User user = User.builder()
            .tenantId(tenantId)
            .email(request.getEmail())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .fullName(request.getFullName())
            .phone(request.getPhone())
            .employeeCode(request.getEmployeeCode())
            .organizationUnitId(request.getOrganizationUnitId())
            .status(User.Status.ACTIVE)
            .build();
            
        user = userRepository.save(user);

        String firstRoleCode = null;
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            List<Role> roles = roleRepository.findAllByIdAndTenantId(request.getRoleIds(), tenantId);
            for (Role role : roles) {
                userRepository.assignRole(user.getId(), role.getId());
                if (firstRoleCode == null) firstRoleCode = role.getCode();
            }
            user.setRoles(roles);
        }

        eventPublisher.publishUserCreated(tenantId, user.getId(), user.getEmail(), firstRoleCode);
        return user;
    }

    public Page<User> listUsers(User.Status status, String role, Pageable pageable) {
        Page<User> users = userRepository.findAllByTenantId(getTenantId(), status, role, pageable);
        users.forEach(u -> u.setRoles(userRepository.findRolesByUserId(u.getId())));
        return users;
    }

    public User getUser(UUID id) {
        User user = userRepository.findByIdAndTenantId(id, getTenantId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setRoles(userRepository.findRolesByUserId(id));
        return user;
    }

    @Transactional
    public User updateUser(UUID id, UpdateUserRequest request) {
        User user = getUser(id);
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setEmployeeCode(request.getEmployeeCode());
        user.setOrganizationUnitId(request.getOrganizationUnitId());
        return userRepository.save(user);
    }

    @Transactional
    public User assignRoles(UUID id, List<UUID> roleIds) {
        User user = getUser(id);
        userRepository.deleteRoles(id);
        List<Role> roles = roleRepository.findAllByIdAndTenantId(roleIds, getTenantId());
        for (Role role : roles) {
            userRepository.assignRole(id, role.getId());
        }
        return getUser(id);
    }

    public Map<UUID, UUID> getTenantIdsForUsers(List<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) return Collections.emptyMap();
        
        String inSql = String.join(",", Collections.nCopies(userIds.size(), "?"));
        String sql = "SELECT id, tenant_id FROM users WHERE id IN (" + inSql + ")";
        
        return jdbcTemplate.query(sql, userIds.toArray(), (rs, rowNum) -> {
            String tenantIdStr = rs.getString("tenant_id");
            return new AbstractMap.SimpleEntry<>(
                UUID.fromString(rs.getString("id")),
                tenantIdStr != null ? UUID.fromString(tenantIdStr) : null
            );
        }).stream()
          .filter(entry -> entry.getValue() != null)
          .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }


    @Transactional
    public User deactivateUser(UUID id) {
        User user = getUser(id);
        user.setStatus(User.Status.INACTIVE);
        invalidateSessions(id);
        return userRepository.save(user);
    }

    @Transactional
    public User lockUser(UUID id) {
        User user = getUser(id);
        user.setStatus(User.Status.LOCKED);
        invalidateSessions(id);
        return userRepository.save(user);
    }

    @Transactional
    public User updateUserStatus(UUID id, User.Status status) {
        User user = getUser(id);
        user.setStatus(status);
        user = userRepository.save(user);

        if (status == User.Status.RESIGNED || status == User.Status.TERMINATED || status == User.Status.SUSPENDED) {
            jdbcTemplate.update("UPDATE refresh_tokens SET is_revoked = true WHERE user_id = ?", id);
            invalidateSessions(id);
        }
        return user;
    }

    @Transactional
    public User unlockUser(UUID id) {
        User user = getUser(id);
        user.setStatus(User.Status.ACTIVE);
        return userRepository.save(user);
    }

    @Transactional
    public User inviteUser(InviteUserRequest request) {
        UUID tenantId = getTenantId();
        
        if (userRepository.findByEmailAndTenantId(request.getEmail(), tenantId).isPresent()) {
            throw new RuntimeException("Email already exists in this tenant");
        }

        User user = User.builder()
            .tenantId(tenantId)
            .email(request.getEmail())
            .status(User.Status.INVITED)
            .build();
            
        user = userRepository.save(user);

        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            List<Role> roles = roleRepository.findAllByIdAndTenantId(request.getRoleIds(), tenantId);
            for (Role role : roles) {
                userRepository.assignRole(user.getId(), role.getId());
            }
            user.setRoles(roles);
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(request.getEmail());
            message.setSubject("Invitation to Join KEMP System");
            message.setText("You have been invited to join the KEMP System. Please register your account.");
            mailSender.send(message);
            log.info("Invitation email sent to: {}", request.getEmail());
        } catch (Exception e) {
            log.error("Failed to send invitation email to {}", request.getEmail(), e);
        }
        
        return user;
    }

    private void invalidateSessions(UUID userId) {
        redisTemplate.delete("session:" + userId.toString());
    }

    private UUID getTenantId() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("TenantContext is empty");
        }
        return tenantId;
    }
}
