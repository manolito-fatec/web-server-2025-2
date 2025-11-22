package com.pardal.app.repository.terms;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pardal.app.entity.terms.TermsOfUse;

public interface TermsOfUseRepository extends JpaRepository<TermsOfUse, Integer>
{
    Optional<TermsOfUse> findTopByOrderByCreatedAtDesc();
}
