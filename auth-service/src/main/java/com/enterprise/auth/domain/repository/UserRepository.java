package com.enterprise.auth.domain.repository;

import com.enterprise.auth.domain.model.User;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findByEmail(String email);
    Optional<User> findById(java.util.UUID id);
    void save(User user);
}
