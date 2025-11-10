package com.pardal.app.controllers;

import com.pardal.app.entity.dto.insights.InsightsDataDto;
import com.pardal.app.entity.dto.insights.InsightsFilterDto;
import com.pardal.app.service.insights.InsightsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/insights")
@RequiredArgsConstructor
@Slf4j
public class InsightsController {

    private final InsightsService insightsService;
    private final String TITLE = "title";

    @Operation(summary = "Busca todos os insights centralizados (SLA, Sazonalidade, Produto, Pareto)",
            description = "Retorna um objeto com todos os dados necessários para a aba Insights, filtrados por Cliente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Insights extraídos com sucesso."),
            @ApiResponse(responseCode = "400", description = "Parâmetros de filtro inválidos."),
            @ApiResponse(responseCode = "404", description = "Recurso não encontrado (ex: Cliente inválido)."),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor ao tentar buscar os insights.")
    })
    @GetMapping("/data")
    public ResponseEntity<?> getAllInsightsData(
            @Parameter(description = "DTO de filtro. O 'clientId' é opcional/nulo para a opção 'Todos'.")
            InsightsFilterDto filters
    ) {
        MDC.put(TITLE,"Busca Insights");
        try {
            InsightsDataDto response = insightsService.getAllInsightsData(filters);
            log.info("Buscar por insight feita com sucesso.");
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException noSuchElementException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException runtimeException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal Server Error: " + runtimeException.getMessage());
        }finally {
            MDC.remove(TITLE);
        }
    }

}
