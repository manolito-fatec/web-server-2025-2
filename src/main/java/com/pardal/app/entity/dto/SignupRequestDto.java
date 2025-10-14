package com.pardal.app.entity.dto;

import lombok.*;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignupRequestDto {
    private String name;
    private String email;
    private String phone;
    private String password;
}