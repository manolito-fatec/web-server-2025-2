package com.pardal.app.service.terms;

import com.pardal.app.entity.dto.terms.NewTermDto;
import com.pardal.app.entity.dto.terms.UseOfTermLoginDto;
import com.pardal.app.entity.terms.TermsOfUse;

public interface TermsOfUseService
{
     public void createNewTerm(NewTermDto newTerm);
     public TermsOfUse getCurrentlyTerm();
     public UseOfTermLoginDto contractIsActive( Integer userId);
}
