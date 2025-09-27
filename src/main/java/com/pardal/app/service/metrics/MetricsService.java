package com.pardal.app.service.metrics;

import com.pardal.app.entity.dto.ChartDto;
import com.pardal.app.entity.dto.DashboardFilterDto;
import com.pardal.app.entity.dto.FilterDataDto;

public interface MetricsService
{
    public FilterDataDto getFilterData(int pPage, int pPageSize);

    ChartDto getAllChartData(DashboardFilterDto filters);
}
