package com.pardal.app.service.Metrics;

import com.pardal.app.entity.Dto.FilterDataDto;

public interface MetricsService
{
    public FilterDataDto getFilterData(int page, int pageSize);
}
