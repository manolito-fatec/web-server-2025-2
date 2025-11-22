package com.pardal.app.entity.dto.auth;

import com.pardal.app.entity.dto.terms.RegisterAndUpdateCheckDto;

import lombok.*;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignupRequestDto extends RegisterAndUpdateCheckDto {
    private String name;
    private String email;
    private String phone;
    private String password;
}