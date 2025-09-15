package com.pardal.app.service.Metrics;

import java.time.LocalDateTime;

import com.pardal.app.entity.Dto.FilterDataDto;

public interface MetricsService
{
    public FilterDataDto getFilterData(int pPage, int pPageSize);

    public String getAllChartData(Integer pProductId, Integer pCustomerId, LocalDateTime pFromDate,LocalDateTime pToDate);
}
