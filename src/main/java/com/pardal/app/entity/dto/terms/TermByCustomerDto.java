package com.pardal.app.entity.dto.terms;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TermByCustomerDto 
{
      private Integer userId;
      private TermOfUseDto term;
      private List<CheckResponseDto> checks;
}
