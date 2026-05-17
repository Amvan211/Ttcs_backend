package com.bookstores.service.impl;

import com.bookstores.DTO.AuthResponse;
import com.bookstores.DTO.LoginRequest;
import com.bookstores.DTO.RegisterRequest;
import com.bookstores.common.ApiRoleNames;
import com.bookstores.common.DomainConstants;
import com.bookstores.entity.Partner;
import com.bookstores.entity.Role;
import com.bookstores.entity.User;
import com.bookstores.repository.PartnerRepository;
import com.bookstores.repository.RoleRepository;
import com.bookstores.repository.UserRepository;
import com.bookstores.security.JwtUtils;
import com.bookstores.service.AuthService;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PartnerRepository partnerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PartnerRepository partnerRepository,
            PasswordEncoder passwordEncoder,
            JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.partnerRepository = partnerRepository;
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
                .avatarUrl(u.getAvatarUrl())
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
                .avatarUrl(u.getAvatarUrl())
                .build();
    }

    @Override
    public void changePassword(Integer userId, String oldPassword, String newPassword) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (!passwordEncoder.matches(oldPassword, u.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mật khẩu hiện tại không đúng");
        }
        u.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(u);
    }

    @Override
    public void updateProfile(Integer userId, com.bookstores.DTO.UpdateProfileRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (req.getFullName() != null) user.setFullName(req.getFullName());
        if (req.getMail() != null) user.setMail(req.getMail());
        if (req.getPhone() != null) user.setPhone(req.getPhone());

        userRepository.save(user);

        if (user.getRole() != null && "PARTNER".equalsIgnoreCase(user.getRole().getRoleName())) {
            Optional<Partner> partnerOpt = partnerRepository.findByUser_Id(user.getId());
            if (partnerOpt.isPresent()) {
                Partner partner = partnerOpt.get();
                if (req.getStoreName() != null) partner.setStoreName(req.getStoreName());
                if (req.getAddress() != null) partner.setAddress(req.getAddress());
                partnerRepository.save(partner);
            }
        }
    }
}
