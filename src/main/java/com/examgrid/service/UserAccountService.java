package com.examgrid.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.examgrid.entity.AppUser;
import com.examgrid.entity.Role;
import com.examgrid.repository.AppUserRepository;

@Service
public class UserAccountService {

    public static final String DEFAULT_PASSWORD = "Welcome@123";

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAccountService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void createAccountIfMissing(String username, String rawPassword, Role role) {
        if (!appUserRepository.existsByUsernameIgnoreCase(username)) {
            appUserRepository.save(new AppUser(username.toUpperCase(), passwordEncoder.encode(rawPassword), role));
        }
    }

    @Transactional
    public void createDefaultAccount(String username, Role role) {
        createAccountIfMissing(username, DEFAULT_PASSWORD, role);
    }

    @Transactional
    public void deleteAccount(String username) {
        appUserRepository.deleteByUsernameIgnoreCase(username);
    }
}
