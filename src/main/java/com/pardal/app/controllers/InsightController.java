package com.pardal.app.controllers;

import com.pardal.app.entity.TicketInsight;
import com.pardal.app.service.insight.InsightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/insights")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class InsightController {

    private final InsightService insightService;

    /**
     * Endpoint to fetch the latest AI insights for a given company, grouped by product.
     * Request example: GET /api/insights/company/9
     *
     * @param companyId The ID of the company for which insights will be fetched.
     * @return A ResponseEntity with a list of insights (200 OK) or an empty body (404 Not Found) if no insights exist.
     */
    @Operation(summary = "Busca os insights mais recentes por empresa", description = "Retorna o insight mais recente para cada produto de uma empresa específica, com base no timestamp mais recente (dth).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de insights recuperada com sucesso."),
            @ApiResponse(responseCode = "404", description = "Nenhum insight encontrado para o ID da empresa fornecido."),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor ao buscar os insights.")
    })
    @GetMapping("/company/{companyId}")
    public ResponseEntity<?> getInsightsByCompanyId(
            @Parameter(description = "ID of the company to fetch insights for.", example = "1")
            @PathVariable Integer companyId) {
        try {
            List<TicketInsight> insights = insightService.findLatestByCompanyId(companyId);
            return ResponseEntity.ok().body(insights);
        } catch (NoSuchElementException noSuchElementException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (RuntimeException runtimeException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error: " + runtimeException.getMessage());
        }
    }
}

