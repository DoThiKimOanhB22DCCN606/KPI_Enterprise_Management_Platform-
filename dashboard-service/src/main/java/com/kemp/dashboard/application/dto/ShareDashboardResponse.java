package com.kemp.dashboard.application.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShareDashboardResponse {
    private String publicToken;
    private String shareUrl;
}
