package com.pardal.app.entity.dto.metrics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MetricsPdfRequestDto extends DashboardFilterDto{
    private List<String> graphImagesBase64;
    private String reportTitle;
}
