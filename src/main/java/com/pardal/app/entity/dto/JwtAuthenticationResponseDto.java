package com.pardal.app.entity.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JwtAuthenticationResponseDto {
    private String token;
}