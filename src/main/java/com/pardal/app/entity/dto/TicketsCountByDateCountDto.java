package com.pardal.app.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TicketsCountByDateCountDto {
    private LocalDate date;
    private Long totalTickets;
}
