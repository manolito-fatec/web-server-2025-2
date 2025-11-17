package com.pardal.app.entity.terms;

import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserTermsAcceptanceId
{
    private Integer userId; 
    private Integer terms; 
    private Integer check; 
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserTermsAcceptanceId that = (UserTermsAcceptanceId) o;
        return Objects.equals(userId, that.userId) &&
               Objects.equals(terms, that.terms) &&
               Objects.equals(check, that.check);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, terms, check);
    }
}
