package com.kemp.organization.infrastructure.persistence;

import com.kemp.organization.domain.model.OrgUnitType;
import jakarta.persistence.Column;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import jakarta.persistence.Entity;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import jakarta.persistence.EnumType;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import jakarta.persistence.Enumerated;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import jakarta.persistence.Id;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import jakarta.persistence.Table;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import java.util.UUID;
import lombok.Data;

@Entity
@Table(name = "organization_units")
@SQLDelete(sql = "UPDATE organization_units SET deleted_at = CURRENT_TIMESTAMP WHERE id=?")
@SQLRestriction("deleted_at IS NULL")
@Data
public class OrgUnitEntity {
    @Id
    private UUID id;
    private UUID tenantId;
    private UUID parentId;
    
    @Enumerated(EnumType.STRING)
    private OrgUnitType type;
    
    private String code;
    private String name;
    private UUID managerUserId;
    
    @Column(columnDefinition = "ltree")
    @org.hibernate.annotations.ColumnTransformer(write = "?::ltree")
    private String path;
    
    private Integer level;
    private Boolean active;

    @Column(name = "deleted_at")
    private java.time.Instant deletedAt;
}
