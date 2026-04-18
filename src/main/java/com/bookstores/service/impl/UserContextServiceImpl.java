package com.bookstores.service.impl;

import com.bookstores.entity.User;
import com.bookstores.repository.UserRepository;
import com.bookstores.service.UserContextService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserContextServiceImpl implements UserContextService {

    private final UserRepository userRepository;

    public UserContextServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User requireCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
            throw new IllegalStateException("Unauthorized");
        }
        return userRepository
                .findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalStateException("User not found"));
    }
}
