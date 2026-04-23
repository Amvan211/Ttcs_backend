package com.bookstores.DTO;

import lombok.Data;

@Data
public class AdminUserUpsertRequest {
    private String username;
    private String password;
    private String fullName;
    private String mail;
    private String phone;
    private String status;
    private String roleName;
    private String avatarUrl;
}
