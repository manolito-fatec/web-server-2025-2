package com.pardal.app.entity.dto.terms;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CheckResponseDto extends CheckInRegisterAndUpdateDto{
    private Boolean check;

    public CheckResponseDto(Integer checkId, String label, Boolean required, Boolean check) {
        super(checkId, label, required); 
        this.check = check;
    }
}