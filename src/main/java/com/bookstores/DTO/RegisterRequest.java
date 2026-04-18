package com.bookstores.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank private String username;

    @NotBlank private String password;

    @NotBlank @Email private String mail;

    private String fullName;

    private String phone;
}
