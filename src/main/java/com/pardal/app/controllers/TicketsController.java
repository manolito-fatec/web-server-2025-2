package com.pardal.app.controllers;

import com.pardal.app.entity.dto.TicketsByProductsCountDto;
import com.pardal.app.service.tickets.TicketsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketsController {

    private final TicketsService ticketsService;

    @Operation(summary = "Busca a contagem de tickets agrupados por produto", description = "Retorna uma lista com a contagem total de tickets para cada produto.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contagem de tickets extraída com sucesso."),
            @ApiResponse(responseCode = "404", description = "Nenhum ticket ou produto encontrado."),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor ao tentar buscar a contagem de tickets.")
    })
    @GetMapping("/by-product")
    public ResponseEntity<?> getTicketsByProduct() {
        try {
            List<TicketsByProductsCountDto> result = ticketsService.getTicketsCountGroupedByProduct();
            if (result.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Found no tickets");
            }
            return ResponseEntity.ok(result);
        } catch (NoSuchElementException noSuchElementException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Found no tickets");
        } catch (RuntimeException runtimeException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(runtimeException.getMessage());
        }
    }
}
