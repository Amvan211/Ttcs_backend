package com.bookstores.controller;

import com.bookstores.DTO.AuthResponse;
import com.bookstores.DTO.LoginRequest;
import com.bookstores.DTO.RegisterRequest;
import com.bookstores.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bookstores.DTO.ChangePasswordRequest;
import com.bookstores.service.UserContextService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserContextService userContextService;

    public AuthController(AuthService authService, UserContextService userContextService) {
        this.authService = authService;
        this.userContextService = userContextService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/password")
    public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        var user = userContextService.requireCurrentUser();
        authService.changePassword(user.getId(), request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok("Đổi mật khẩu thành công");
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/profile")
    public ResponseEntity<String> updateProfile(@RequestBody com.bookstores.DTO.UpdateProfileRequest request) {
        var user = userContextService.requireCurrentUser();
        authService.updateProfile(user.getId(), request);
        return ResponseEntity.ok("Cập nhật hồ sơ thành công");
    }
}
