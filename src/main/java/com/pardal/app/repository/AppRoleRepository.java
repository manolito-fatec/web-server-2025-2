package com.pardal.app.repository;

import com.pardal.app.entity.AppRole;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.Repository;

import java.util.Set;

public interface AppRoleRepository extends JpaRepository<AppRole, Long> {
    Set<AppRole> findById(Integer id);

    @NotNull AppRole getAppRoleById(Integer id);
}
