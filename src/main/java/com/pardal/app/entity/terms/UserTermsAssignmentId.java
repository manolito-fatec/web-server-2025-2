package com.pardal.app.entity.terms;

import java.util.Objects;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class UserTermsAssignmentId
{
    private Integer userId; 
    private Integer terms;

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserTermsAssignmentId that = (UserTermsAssignmentId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(terms, that.terms);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(userId, terms);
    }
}
