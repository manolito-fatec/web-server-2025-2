package com.pardal.app.controller;

import com.pardal.app.service.Tickets.TicketsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/tickets")
public class TicketsController {

    @Autowired
    private TicketsService ticketsService;

    /**
     * Retrieves a count of tickets based on optional filter criteria.
     *
     * @author AndreWakugawa
     *
     * @param productId Optional product ID to filter by.
     * @param clientId  Optional client (company) ID to filter by.
     * @param dateMin   Optional start date (inclusive) for creation date filtering.
     * @param dateMax   Optional end date (inclusive) for creation date filtering.
     * @return A response entity containing the total count of matching tickets.
     */
    @GetMapping("/count")
    public ResponseEntity<Object> getFilteredTicketsCount(
            @RequestParam(name = "productId", required = false) Integer productId,
            @RequestParam(name = "clientId", required = false) Integer clientId,
            @RequestParam(name = "dateMin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateMin,
            @RequestParam(name = "dateMax", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateMax) {

        try {
            return ResponseEntity.ok().body(ticketsService.getTicketsCount(
                    Optional.ofNullable(productId),
                    Optional.ofNullable(clientId),
                    Optional.ofNullable(dateMin),
                    Optional.ofNullable(dateMax)
            ));
        } catch (NoSuchElementException noSuchElementException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException runtimeException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error " + runtimeException.getMessage());
        }
    }
}