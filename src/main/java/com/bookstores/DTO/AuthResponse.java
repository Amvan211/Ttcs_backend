package com.bookstores.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String tokenType;
    private Integer userId;
    private String username;
    private String roleName;
    /** Display name — khớp trường name trên front_end. */
    private String fullName;
    private String mail;
    private String avatarUrl;
}
