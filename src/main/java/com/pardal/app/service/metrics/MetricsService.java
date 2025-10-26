package com.pardal.app.service.metrics;

import com.pardal.app.entity.dto.metrics.ChartDto;
import com.pardal.app.entity.dto.metrics.DashboardFilterDto;
import com.pardal.app.entity.dto.metrics.FilterMetricsDataDto;

public interface MetricsService
{
    public FilterMetricsDataDto getFilterData(int pPage, int pPageSize);

    ChartDto getAllChartData(DashboardFilterDto filters);
}
