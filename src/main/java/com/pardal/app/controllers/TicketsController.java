package com.pardal.app.controllers;

import com.pardal.app.entity.Dto.TicketsByProductsCountDto;
import com.pardal.app.service.Tickets.TicketsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketsController {

    private final TicketsService ticketsService;

    @GetMapping("/by-product")
    public ResponseEntity<List<TicketsByProductsCountDto>> getTicketsByProduct() {
        try {
            return ResponseEntity.ok(ticketsService.getTicketsCountGroupedByProduct());
        } catch (NoSuchElementException noSuchElementException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
