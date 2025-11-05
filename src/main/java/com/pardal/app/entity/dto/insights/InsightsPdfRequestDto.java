package com.pardal.app.entity.dto.insights;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class InsightsPdfRequestDto extends InsightsFilterDto{
    private List<String> graphImagesBase64;
    private String reportTitle;
}
