package com.pardal.app.repository;

import com.pardal.app.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);

    Optional<AppUser> findById(Integer id);

    Optional<AppUser> getAppUserByVerificationToken(String verificationToken);

    Optional<AppUser> getAppUserByEmail(String email);
}
