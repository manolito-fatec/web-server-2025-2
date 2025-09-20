package com.pardal.app.controllers;

import com.pardal.app.service.tickets.TicketsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.pardal.app.service.metrics.MetricsService;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
public class MetricsController {

    private final TicketsService ticketsService;
    private final MetricsService metricsService;

    @Operation(summary = "Busca dados de filtro de forma paginada", description = "Retorna uma lista paginada de dados de filtro com base nos parâmetros 'page' e 'size'.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dados de filtro paginados extraídos com sucesso."),
            @ApiResponse(responseCode = "400", description = "Parâmetros de paginação inválidos."),
            @ApiResponse(responseCode = "404", description = "Recurso não encontrado."),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor ao tentar buscar os dados de filtro.")
    })
    @GetMapping()
    public ResponseEntity<?> getFilterData(
            @Parameter(description = "Numero da pagina (1-based)", example = "1")
            @RequestParam(defaultValue = "1") int page,

            @Parameter(description = "Numero de itens por pagina (default = 10)", example = "10")
            @RequestParam(defaultValue = "10") int size
    ){
        try {
            return ResponseEntity.ok().body(metricsService.getFilterData(page, size)
            );
        } catch (NoSuchElementException noSuchElementException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException runtimeException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error " + runtimeException.getMessage());
        }

    }

    @Operation(summary = "Busca informações para preenchimento do gráfico", description = "Retorna informações para serem usadas para exibição em forma de gráfico e em cards")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso."),
            @ApiResponse(responseCode = "400", description = "Parâmetros de busca inválidos."),
            @ApiResponse(responseCode = "404", description = "Recurso não encontrado."),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor ao tentar buscar os dados do gráfico")
    })
    @GetMapping("/chart")
    public ResponseEntity<?> getAllChartData(
            @Parameter(description = "Id do produto", example = "1")
            @RequestParam(name = "productId", required = false)
            Integer productId,

            @Parameter(description = "Id do cliente", example = "1")
            @RequestParam(name="customerId", required = false)
            Integer customerId,

            @Parameter(description = "Data de inicio", example = "2025-09-15T13:45:30")
            @RequestParam(name = "fromDate", required = false)
            LocalDateTime fromDate,

            @Parameter(description = "Data de final", example = "2025-09-15T13:45:30")
            @RequestParam(name="toDate", required = false)
            LocalDateTime toDate
    ){
        try {
            return ResponseEntity.ok().body(metricsService.getAllChartData(
                    Optional.ofNullable(productId),
                    Optional.ofNullable(customerId),
                    Optional.ofNullable(fromDate),
                    Optional.ofNullable(toDate)
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
