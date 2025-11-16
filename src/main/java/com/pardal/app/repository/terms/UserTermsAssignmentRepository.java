package com.pardal.app.repository.terms;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pardal.app.entity.terms.UserTermsAssignment;
import com.pardal.app.entity.terms.UserTermsAssignmentId;

public interface UserTermsAssignmentRepository extends JpaRepository<UserTermsAssignment, UserTermsAssignmentId>
{
    @Query("SELECT uta FROM UserTermsAssignment uta " +
            "WHERE uta.userId.id = :userId " +
            "AND uta.terms.termsId = (SELECT MAX(t.termsId) FROM TermsOfUse t)")
     Optional<UserTermsAssignment> findByUserIdAndMostRecentTerm(@Param("userId") Integer userId);
}
