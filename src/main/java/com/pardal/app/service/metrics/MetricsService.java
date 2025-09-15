package com.pardal.app.service.metrics;

import java.time.LocalDateTime;

import com.pardal.app.entity.dto.ChartDto;
import com.pardal.app.entity.dto.FilterDataDto;

public interface MetricsService
{
    public FilterDataDto getFilterData(int pPage, int pPageSize);

    public ChartDto getAllChartData(Integer pProductId, Integer pCustomerId, LocalDateTime pFromDate,LocalDateTime pToDate);
}
