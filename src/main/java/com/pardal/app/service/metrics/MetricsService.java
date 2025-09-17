package com.pardal.app.service.metrics;

import java.time.LocalDateTime;
import java.util.Optional;

import com.pardal.app.entity.dto.ChartDto;
import com.pardal.app.entity.dto.FilterDataDto;

public interface MetricsService
{
    public FilterDataDto getFilterData(int pPage, int pPageSize);

    public ChartDto getAllChartData(Optional<Integer> pProductId,
            Optional<Integer> pCustomerId,
            Optional<LocalDateTime> pFromDate,
            Optional<LocalDateTime> pToDate);
}
