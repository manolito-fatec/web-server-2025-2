package com.pardal.app.entity.dto.terms;

import com.pardal.app.entity.terms.TermsOfUse;

public record UseOfTermLoginDto( Boolean isActive,
        TermsOfUse term) {}
