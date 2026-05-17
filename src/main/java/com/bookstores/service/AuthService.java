package com.bookstores.service;

import com.bookstores.DTO.AuthResponse;
import com.bookstores.DTO.LoginRequest;
import com.bookstores.DTO.RegisterRequest;

public interface AuthService {

    AuthResponse login(LoginRequest req);

    AuthResponse register(RegisterRequest req);

    void changePassword(Integer userId, String oldPassword, String newPassword);

    void updateProfile(Integer userId, com.bookstores.DTO.UpdateProfileRequest req);
}
