package com.bookstores.DTO;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String fullName;
    private String mail;
    private String phone;
    private String storeName;
    private String address;
}
