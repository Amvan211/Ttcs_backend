package com.bookstores.bootstrap;

import com.bookstores.common.DomainConstants;
import com.bookstores.entity.Role;
import com.bookstores.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public DataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) {
        ensureRole(DomainConstants.ROLE_CUSTOMER);
        ensureRole(DomainConstants.ROLE_PARTNER);
        ensureRole(DomainConstants.ROLE_ADMIN);
    }

    private void ensureRole(String name) {
        roleRepository.findByRoleName(name).orElseGet(() -> roleRepository.save(Role.builder().roleName(name).build()));
    }
}
