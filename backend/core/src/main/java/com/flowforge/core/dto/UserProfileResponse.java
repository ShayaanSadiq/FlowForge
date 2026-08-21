package com.flowforge.core.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileResponse {

    private String userId;
    private String email;
    private String displayName;
}
