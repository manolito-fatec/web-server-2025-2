package com.pardal.app.repository;

import com.pardal.app.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<AppUser, Integer> {
}