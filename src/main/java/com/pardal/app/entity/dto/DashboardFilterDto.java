package com.pardal.app.entity.dto;

import com.pardal.app.enums.GroupingPeriods;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class DashboardFilterDto {
    private Integer productId;
    private Integer customerId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private GroupingPeriods periods;
}
