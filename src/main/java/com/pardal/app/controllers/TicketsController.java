package com.pardal.app.controller;

import com.pardal.app.service.Tickets.TicketsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import java.util.Optional;

@RestController
@RequestMapping("/api/tickets")
public class TicketsController {

    @Autowired
    private TicketsService ticketsService;

    @Operation(summary = "Busca a contagem de tickets com base em filtros", description = "Retorna o número total de tickets que correspondem aos critérios de filtro opcionais, como ID do produto, ID do cliente e um intervalo de datas de criação.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contagem de tickets retornada com sucesso."),
            @ApiResponse(responseCode = "400", description = "Parâmetros de filtro inválidos."),
            @ApiResponse(responseCode = "404", description = "Recurso não encontrado."),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor ao buscar a contagem de tickets.")
    })
    @GetMapping("/count")
    public ResponseEntity<Object> getFilteredTicketsCount(
            @Parameter(description = "ID do produto para filtrar os tickets", example = "1")
            @RequestParam(name = "productId", required = false) Integer productId,

            @Parameter(description = "ID do cliente (empresa) para filtrar os tickets", example = "101")
            @RequestParam(name = "clientId", required = false) Integer clientId,

            @Parameter(description = "Data de início para filtrar por data de criação. Formato ISO: YYYY-MM-DDTHH:mm:ss", example = "2025-09-16T00:00:00")
            @RequestParam(name = "dateMin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateMin,

            @Parameter(description = "Data final para filtrar por data de criação. Formato ISO: YYYY-MM-DDTHH:mm:ss", example = "2025-09-16T23:59:59")
            @RequestParam(name = "dateMax", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateMax) {

        try {
            long count = ticketsService.getTicketsCount(
                    Optional.ofNullable(productId),
                    Optional.ofNullable(clientId),
                    Optional.ofNullable(dateMin),
                    Optional.ofNullable(dateMax)
            );
            return ResponseEntity.ok().body(count);
        } catch (NoSuchElementException noSuchElementException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException runtimeException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error " + runtimeException.getMessage());
        }
    }
}