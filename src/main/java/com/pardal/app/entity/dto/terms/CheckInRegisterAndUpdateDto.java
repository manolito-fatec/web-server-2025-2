package com.pardal.app.entity.dto.terms;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor 
@NoArgsConstructor 
public class CheckInRegisterAndUpdateDto {
    private Integer checkId;
    private String label;
    private Boolean required;
}
