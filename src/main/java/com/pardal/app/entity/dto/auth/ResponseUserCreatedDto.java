package com.pardal.app.entity.dto.auth;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseUserCreatedDto {
    private Integer id;
    private String name;
    private String email;
    private String role;
}
