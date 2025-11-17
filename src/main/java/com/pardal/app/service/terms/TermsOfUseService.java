package com.pardal.app.service.terms;

import com.pardal.app.entity.dto.terms.NewTermDto;
import com.pardal.app.entity.dto.terms.RegisterAndUpdateCheckDto;
import com.pardal.app.entity.dto.terms.UseOfTermLoginDto;
import com.pardal.app.entity.terms.TermsOfUse;

public interface TermsOfUseService
{
     public void createNewTerm(NewTermDto newTerm);
     public TermsOfUse getCurrentlyTerm();
     public UseOfTermLoginDto contractIsActive( Integer userId);
     public void updateContract(RegisterAndUpdateCheckDto register);
     public void RegisterContract(RegisterAndUpdateCheckDto register);
     public void termNotPending(Boolean validated);
}
