package com.pardal.app.controllers;

import com.pardal.app.entity.documents.Forecaster;
import com.pardal.app.entity.documents.TicketInsight;
import com.pardal.app.entity.dto.insights.InsightsDataDto;
import com.pardal.app.entity.dto.insights.InsightsFilterDto;
import com.pardal.app.entity.dto.insights.InsightsPdfRequestDto;
import com.pardal.app.entity.dto.insights.SlaPredictionResponseDto;
import com.pardal.app.entity.dto.metrics.TicketsBySubcategoryCountDto;
import com.pardal.app.service.export.CsvExportService;
import com.pardal.app.service.export.PdfExportService;
import com.pardal.app.service.insights.InsightsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/insights")
@RequiredArgsConstructor
public class InsightsController {

    private final InsightsService insightsService;
    private final CsvExportService csvExportService;
    private final PdfExportService pdfExportService;

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
        try {
            InsightsDataDto response = insightsService.getAllInsightsData(filters);
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException noSuchElementException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException runtimeException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal Server Error: " + runtimeException.getMessage());
        }
    }

    @Operation(summary = "Exporta todos os insights filtrados para CSV",
            description = "Gera um arquivo ZIP contendo 4 CSVs (um para cada insight) com os dados filtrados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Arquivo ZIP com os CSVs gerado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Parâmetros de filtro inválidos."),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor ao gerar o arquivo.")
    })
    @GetMapping("/export/csv")
    public void exportInsightsToCsv(
            @Parameter(description = "DTO de filtro.")
            InsightsFilterDto filters,
            HttpServletResponse response) throws IOException {
        InsightsDataDto data;
        try {
            data = insightsService.getAllInsightsData(filters);
        } catch (Exception e) {
            response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error fetching insights data: " + e.getMessage());
            return;
        }

        String zipFileName = "insights_export_" + System.currentTimeMillis() + ".zip";
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + zipFileName + "\"");

        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {

            addCsvToZip(zos, "sla_prediction_data.csv", data.getSlaInsightData(), SlaPredictionResponseDto.class);
            addCsvToZip(zos, "seasonality_forecaster_data.csv", data.getSeasonalityInsightData(), Forecaster.class);
            addCsvToZip(zos, "product_insights_data.csv", data.getProductInsightsData(), TicketInsight.class);
            addCsvToZip(zos, "pareto_subcategory_data.csv", data.getParetoInsightData(), TicketsBySubcategoryCountDto.class);

            response.flushBuffer();

        } catch (Exception e) {
            response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error generating export file.");
        }
    }

    private <T> void addCsvToZip(ZipOutputStream zos, String entryName, List<T> dataList, Class<T> type) throws IOException {
        if (dataList == null || dataList.isEmpty()) {
            return;
        }

        ZipEntry entry = new ZipEntry(entryName);
        zos.putNextEntry(entry);

        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(zos, StandardCharsets.UTF_8))) {
            csvExportService.writeCsv(writer, dataList, type);
            writer.flush();
        } catch (RuntimeException e) {
            throw new IOException(e);
        }

        zos.closeEntry();
    }

    @Operation(summary = "Exporta os insights visuais para PDF",
            description = "Recebe imagens Base64 dos gráficos renderizados no frontend (para sincronização visual) e os empacota em um PDF.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF gerado e enviado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Dados de requisição inválidos."),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor ao gerar o PDF.")
    })
    @PostMapping("/export/pdf")
    public ResponseEntity<byte[]> exportInsightsToPdf(
            @RequestBody InsightsPdfRequestDto request
    ) {
        try {
            byte[] pdfBytes = pdfExportService.generateInsightsPdf(request);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            String filename = "insights_report_" + System.currentTimeMillis() + ".pdf";
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (IOException ioException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("IO Error: " + ioException.getMessage()).getBytes(StandardCharsets.UTF_8));
        } catch (RuntimeException runtimeException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Internal Server Error: " + runtimeException.getMessage()).getBytes(StandardCharsets.UTF_8));
        }
    }
}
