package com.pardal.app.entity.dto.insights;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class InsightsFilterDto {
    private List<Integer> customerIds;
    private List<Integer> productIds;
}