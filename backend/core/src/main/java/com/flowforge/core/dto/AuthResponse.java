package com.flowforge.core.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {

    private String token;
    private String userId;
    private String email;
    private String displayName;
}
