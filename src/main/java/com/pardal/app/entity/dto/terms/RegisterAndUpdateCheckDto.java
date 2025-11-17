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
public class RegisterAndUpdateCheckDto
{
    private Integer userId;
    private Integer termsId;
    private Boolean termAccepted;
    private List<CheckInRegisterAndUpdate> checkList;
}
