package com.pardal.app.service.auth;

import com.pardal.app.entity.dto.auth.JwtAuthenticationResponseDto;
import com.pardal.app.entity.dto.auth.LoginRequestDto;
import com.pardal.app.entity.dto.auth.ResponseUserCreatedDto;
import com.pardal.app.entity.dto.auth.SignupRequestDto;

public interface AuthService {
    ResponseUserCreatedDto signup(SignupRequestDto request);
    ResponseUserCreatedDto verify(String token);
    JwtAuthenticationResponseDto login(LoginRequestDto request);
    ResponseUserCreatedDto approval(Integer userId);
}
