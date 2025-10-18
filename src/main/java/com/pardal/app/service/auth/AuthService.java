package com.pardal.app.service.auth;

import com.pardal.app.entity.dto.JwtAuthenticationResponseDto;
import com.pardal.app.entity.dto.LoginRequestDto;
import com.pardal.app.entity.dto.ResponseUserCreatedDto;
import com.pardal.app.entity.dto.SignupRequestDto;

public interface AuthService {
    ResponseUserCreatedDto signup(SignupRequestDto request);
    ResponseUserCreatedDto verify(String token);
    JwtAuthenticationResponseDto login(LoginRequestDto request);
}
