package com.pardal.app.entity.dto.metrics;

import com.pardal.app.enums.GroupingPeriods;
import com.pardal.app.repository.specification.tickets.TicketsFilters;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DashboardFilterDto extends TicketsFilters {
    private GroupingPeriods periods;
}
