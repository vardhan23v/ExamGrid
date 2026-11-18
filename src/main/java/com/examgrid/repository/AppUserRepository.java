package com.examgrid.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.examgrid.entity.AppUser;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);

    void deleteByUsernameIgnoreCase(String username);
}
