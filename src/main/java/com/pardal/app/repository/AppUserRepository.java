package com.pardal.app.repository;

import com.pardal.app.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findById(Integer id);

    Optional<AppUser> getAppUserByVerificationToken(String verificationToken);

    Optional<AppUser> findByEmailHash(String emailHash);

    Optional<AppUser> getAppUserByEmailHash(String emailHash);

    List<AppUser> findAllByExpireDateIsNull();

    List<AppUser> findAllByEmailHash(String emailHash);

    List<AppUser> findAllByEmailHashAndExpireDateIsNull(String emailHash);

    List<AppUser> findAllByEmailHashIsNotNull();
}
