package com.pardal.app.repository;

import com.pardal.app.entity.AppUser;
import com.pardal.app.entity.User;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface UserRepository extends Repository<User, Integer> {
    List<AppUser> findAll();
}