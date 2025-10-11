package com.pardal.app.repository;

import com.pardal.app.entity.AppUser;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface AppUserRepository extends Repository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);

    Optional<AppUser> findById(Integer id);
}
