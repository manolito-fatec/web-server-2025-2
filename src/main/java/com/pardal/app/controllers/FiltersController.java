package com.pardal.app.controllers;

import com.pardal.app.service.Tickets.TicketsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/filters")
@RequiredArgsConstructor
public class FiltersController {

    private final TicketsService ticketsService;

    @GetMapping()
    public ResponseEntity<?> getNumberOfTickets(
            @RequestParam("productId") Optional<Integer> productId,
            @RequestParam("clientId") Optional<Integer> clientId,
            @RequestParam("dateMin") Optional<LocalDateTime> dateMin,
            @RequestParam("dateMax") Optional<LocalDateTime> dateMax,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "50") Integer limit){
        try {
            return ResponseEntity.ok().body(ticketsService.getTicketsCount(
                    productId,
                    clientId,
                    dateMin,
                    dateMax
                    )
            );
        } catch (NoSuchElementException noSuchElementException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException runtimeException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error " + runtimeException.getMessage());
        }

    }

}
