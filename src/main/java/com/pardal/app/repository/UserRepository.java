package com.pardal.app.repository;

import com.pardal.app.entity.User;
import org.springframework.data.repository.Repository;

public interface UserRepository extends Repository<User, Integer> {
}