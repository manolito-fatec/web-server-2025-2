package com.pardal.app.repository.terms;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pardal.app.entity.terms.UserTermsAcceptance;
import com.pardal.app.entity.terms.UserTermsAcceptanceId;

public interface UserTermsAcceptancedRepository extends JpaRepository<UserTermsAcceptance, UserTermsAcceptanceId>
{
    @Query("SELECT uta FROM UserTermsAcceptance uta " +
            "WHERE uta.userId.id = :userId AND uta.terms.termsId = :termsId")
    List<UserTermsAcceptance> findByUserIdUserIdAndTermsTermsId(@Param("userId") Integer userId, 
        @Param("termsId") Integer termsId);
}
