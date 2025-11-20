package com.pardal.app.entity.dto.terms;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UseOfTermLoginDto {

    private Boolean isActive;
    private TermOfUseDto term;
    private List<CheckResponseDto> checkList;
}
