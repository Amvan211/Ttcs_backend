package com.bookstores.service.impl;

import com.bookstores.DTO.AuthResponse;
import com.bookstores.DTO.LoginRequest;
import com.bookstores.DTO.RegisterRequest;
import com.bookstores.common.ApiRoleNames;
import com.bookstores.common.DomainConstants;
import com.bookstores.entity.Role;
import com.bookstores.entity.User;
import com.bookstores.repository.RoleRepository;
import com.bookstores.repository.UserRepository;
import com.bookstores.security.JwtUtils;
import com.bookstores.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public AuthResponse login(LoginRequest req) {
        String key = req.getUsername().trim();
        User u =
                userRepository
                        .findByUsername(key)
                        .or(() -> userRepository.findByMail(key))
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!passwordEncoder.matches(req.getPassword(), u.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        if (DomainConstants.USER_LOCKED.equalsIgnoreCase(u.getStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account locked");
        }
        String dbRole = u.getRole() != null ? u.getRole().getRoleName() : DomainConstants.ROLE_CUSTOMER;
        String apiRole = ApiRoleNames.toApi(dbRole);
        String token = jwtUtils.generateToken(u.getUsername(), u.getId(), apiRole);
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(u.getId())
                .username(u.getUsername())
                .roleName(apiRole)
                .fullName(u.getFullName())
                .mail(u.getMail())
                .build();
    }

    @Override
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username taken");
        }
        if (userRepository.findByMail(req.getMail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
        Role readerRole =
                roleRepository
                        .findByRoleName(DomainConstants.ROLE_READER)
                        .or(() -> roleRepository.findByRoleName(DomainConstants.ROLE_CUSTOMER))
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Missing role"));
        User u =
                User.builder()
                        .username(req.getUsername())
                        .password(passwordEncoder.encode(req.getPassword()))
                        .mail(req.getMail())
                        .fullName(req.getFullName())
                        .phone(req.getPhone())
                        .status(DomainConstants.USER_ACTIVE)
                        .role(readerRole)
                        .build();
        u = userRepository.save(u);
        String apiRole = ApiRoleNames.toApi(readerRole.getRoleName());
        String token = jwtUtils.generateToken(u.getUsername(), u.getId(), apiRole);
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(u.getId())
                .username(u.getUsername())
                .roleName(apiRole)
                .fullName(u.getFullName())
                .mail(u.getMail())
                .build();
    }
}
