package com.kemp.tenant.application.dto;

import lombok.Data;

@Data
public class UpdateThemeRequest {
    private String primaryColor;
    private String secondaryColor;
    private String fontFamily;
    private String companyName;
    private String tagline;
}
