package com.kemp.user.domain.model;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Permission {
    private UUID id;
    private String code;
    private String resource;
    private String action;
    private String description;
}
