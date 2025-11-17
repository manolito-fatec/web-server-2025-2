package com.pardal.app.controllers;

import com.pardal.app.entity.dto.metrics.ChartDto;
import com.pardal.app.entity.dto.metrics.DashboardFilterDto;
import com.pardal.app.entity.dto.metrics.MetricsPdfRequestDto;
import com.pardal.app.service.export.CsvExportService;
import com.pardal.app.service.export.PdfExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.pardal.app.service.metrics.MetricsService;
import com.pardal.app.util.RequestExceptionHandler;

import java.io.IOException;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
@Slf4j
public class MetricsController {

    private final MetricsService metricsService;
    private final CsvExportService csvExportService;
    private final PdfExportService pdfExportService;

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
        return RequestExceptionHandler.handleRequest("Busca dados de filtro", () -> {
            log.info("Busca dados de filtro de forma paginada");
            return ResponseEntity.ok().body(metricsService.getFilterData(page, size));
        });
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
            @Parameter(description = "Dto de filtro com dados de productId, customerId, startDate, endDate e período de agrupamento.")
            DashboardFilterDto filters
            ){
        return RequestExceptionHandler.handleRequest("Busca Por Métricas", () -> {
            log.info("Busca informações para preenchimento do gráfico");
            return ResponseEntity.ok().body(metricsService.getAllChartData(filters));
        });
    }

    @Operation(summary = "Exporta métricas para CSV",
            description = "Gera um arquivo ZIP contendo CSVs com os dados do Dashboard.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Arquivo ZIP gerado com sucesso."),
            @ApiResponse(responseCode = "500", description = "Erro interno ao gerar arquivo.")
    })
    @GetMapping("/export/csv")
    public void exportMetricsToCsv(
            @Parameter(description = "DTO de filtro.")
            DashboardFilterDto filters,
            HttpServletResponse response) throws IOException {

        ChartDto data;
        try {
            data = metricsService.getAllChartData(filters);
        } catch (Exception e) {
            response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error fetching metrics data: " + e.getMessage());
            return;
        }

        String zipFileName = "metrics_export_" + System.currentTimeMillis() + ".zip";
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + zipFileName + "\"");

        RequestExceptionHandler.handleExport("Exportar Métricas para CSV", response, () -> {
            log.info("Exporta métricas filtradas para CSV");
            csvExportService.exportMetricsZip(data, response.getOutputStream());
        });
    }

    @Operation(summary = "Exporta dashboard de métricas para PDF",
            description = "Recebe imagens Base64 dos gráficos e gera um PDF contendo os KPIs e Gráficos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF gerado com sucesso."),
            @ApiResponse(responseCode = "500", description = "Erro interno ao gerar PDF.")
    })
    @PostMapping("/export/pdf")
    public ResponseEntity<byte[]> exportMetricsToPdf(
            @RequestBody MetricsPdfRequestDto request
    ) {
        String baseFilename = "metrics_report";
        return RequestExceptionHandler.handlePdfFileRequest("Exportar Métricas para PDF", baseFilename, () -> {
            log.info("Exporta dashboard de métricas para PDF");
            return pdfExportService.generateMetricsPdf(request);
        });
    }
}
