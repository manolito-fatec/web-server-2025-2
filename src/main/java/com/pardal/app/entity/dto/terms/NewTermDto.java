package com.pardal.app.entity.dto.terms;

import java.util.List;

import com.pardal.app.entity.terms.TermsCheckItems;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NewTermDto
{
    private String title;
    private String content;
    private List<TermsCheckItems> checkList;
}
