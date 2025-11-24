package com.pardal.app.entity.dto;

import com.pardal.app.entity.AppRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AppUserDto {
    private Integer id;
    private String name;
    private String email;
    private String phone;
    private AppRole role;
    private LocalDate expireDate;
    private String password;
    private boolean emailVerified;

}
