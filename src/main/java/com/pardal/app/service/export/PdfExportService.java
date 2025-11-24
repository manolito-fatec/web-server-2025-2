package com.pardal.app.service.export;

import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;
import com.pardal.app.entity.dto.insights.InsightsPdfRequestDto;
import com.pardal.app.entity.dto.metrics.MetricsPdfRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

@Service
@Slf4j
public class PdfExportService {

    private static final Font.FontFamily DEFAULT_FONT_FAMILY = Font.FontFamily.HELVETICA;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter FILTER_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Generates a PDF document for the Insights dashboard.
     * @param request The DTO containing filters and Base64 encoded images.
     * @return The generated PDF file content as a byte array.
     * @throws IOException If image decoding or PDF writing fails.
     */
    public byte[] generateInsightsPdf(InsightsPdfRequestDto request) throws IOException {
        return generatePdf(
                request.getReportTitle(),
                request.getGraphImagesBase64(),
                (document) -> addInsightsFiltersToDocument(request, document)
        );
    }

    /**
     * Generates a PDF document for the Metrics dashboard.
     * @param request The DTO containing filters and Base64 encoded images.
     * @return The generated PDF file content as a byte array.
     * @throws IOException If image decoding or PDF writing fails.
     */
    public byte[] generateMetricsPdf(MetricsPdfRequestDto request) throws IOException {
        return generatePdf(
                request.getReportTitle(),
                request.getGraphImagesBase64(),
                (document) -> addMetricsFiltersToDocument(request, document));
    }

    @FunctionalInterface
    private interface FilterWriter {
        void write(Document document) throws DocumentException;
    }


    /**
     * Core helper method that manages the lifecycle of the PDF document generation.
     * <p>
     * This method initializes the {@code Document} and {@code PdfWriter}, opens the document,
     * adds the standard title/header, executes the specific filter writing logic provided via
     * the functional interface, adds the images, and finally closes the document.
     * </p>
     *
     * @param title        The title to be displayed at the top of the report.
     * @param images       A list of Base64 encoded strings representing the charts/graphs.
     * @param filterWriter A functional interface that defines how the specific filters (Insights vs Metrics) should be written to the document.
     * @return The final PDF document as a byte array.
     * @throws RuntimeException if any low-level {@code DocumentException} or {@code IOException} occurs.
     */
    private byte[] generatePdf(String title, List<String> images, FilterWriter filterWriter) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, bos);
            document.open();

            setupTitlesToDocument(document, title);

            filterWriter.write(document);

            addImagesToDocument(document, images);

            document.close();
            return bos.toByteArray();
        } catch (Exception e) {
            log.error("Erro Crítico durante a geração de PDF: {}", e.getMessage());
            throw new RuntimeException("Failed to generate PDF document.", e);
        }
    }

    private void setupTitlesToDocument(Document document, String title) throws DocumentException {
        Font titleFont = new Font(DEFAULT_FONT_FAMILY, 18, Font.BOLD, BaseColor.BLACK);
        document.add(new Paragraph(title != null ? title : "Relatório", titleFont));

        Font subTitleFont = new Font(DEFAULT_FONT_FAMILY, 10, Font.ITALIC, BaseColor.GRAY);
        String formattedDateTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);

        document.add(new Paragraph("Gerado em: " + formattedDateTime, subTitleFont));
        document.add(new Paragraph("\n"));
    }

    private void addImagesToDocument(Document document, List<String> base64Images) throws DocumentException {
        if (base64Images != null) {
            for (String base64Image : base64Images) {
                try {
                    Image graphImage = processBase64Image(base64Image, document);

                    document.add(graphImage);
                    document.add(new Paragraph("\n\n"));
                } catch (Exception e) {
                    log.error("Erro ao adicionar a imagem ao PDF: {}", e.getMessage());
                    document.add(new Paragraph(
                            "[Image failed to load: " + e.getMessage() + "]",
                            new Font(DEFAULT_FONT_FAMILY, 8, Font.ITALIC, BaseColor.RED)));
                }
            }
        }
    }

    private void addInsightsFiltersToDocument(InsightsPdfRequestDto request, Document document) throws DocumentException {
        Font filterFont = new Font(DEFAULT_FONT_FAMILY, 10, Font.NORMAL, BaseColor.BLACK);

        document.add(new Paragraph("Filtros aplicados (Insights):", new Font(DEFAULT_FONT_FAMILY, 10, Font.BOLD)));

        String customerIdsStr = formatIdList(request.getCustomerIds());
        document.add(new Paragraph("  IDs de Cliente: " + customerIdsStr, filterFont));

        String productIdsStr = formatIdList(request.getProductIds());
        document.add(new Paragraph("  IDs de Produto: " + productIdsStr, filterFont));

        document.add(new Paragraph("\n"));
    }

    private void addMetricsFiltersToDocument(MetricsPdfRequestDto request, Document document) throws DocumentException {
        Font filterFont = new Font(DEFAULT_FONT_FAMILY, 10, Font.NORMAL, BaseColor.BLACK);
        Font labelFont = new Font(DEFAULT_FONT_FAMILY, 10, Font.BOLD, BaseColor.BLACK);

        document.add(new Paragraph("Filtros aplicados (Métricas):", labelFont));

        String customerTxt = request.getCustomerId() != null ? request.getCustomerId().toString() : "Todos";
        document.add(new Paragraph("  Cliente (ID): " + customerTxt, filterFont));

        String productTxt = request.getProductId() != null ? request.getProductId().toString() : "Todos";
        document.add(new Paragraph("  Produto (ID): " + productTxt, filterFont));

        String fromDate = request.getFromDate() != null ? request.getFromDate().format(FILTER_DATE_FORMATTER) : "Início";
        String toDate = request.getToDate() != null ? request.getToDate().format(FILTER_DATE_FORMATTER) : "Fim";
        document.add(new Paragraph("  Período: " + fromDate + " até " + toDate, filterFont));

        String period = request.getPeriods() != null ? request.getPeriods().name() : "Padrão";
        document.add(new Paragraph("  Agrupamento: " + period, filterFont));

        document.add(new Paragraph("\n"));
    }

    private Image processBase64Image(String base64Image, Document document) throws DocumentException, IOException {
        String base64Content = base64Image.replaceAll("^data:image/[^;]+;base64,", "");

        byte[] imageBytes = Base64.getDecoder().decode(base64Content);
        Image graphImage = Image.getInstance(imageBytes);

        float documentWidth = document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin();

        graphImage.scaleToFit(documentWidth, document.getPageSize().getHeight() - document.topMargin() - document.bottomMargin());

        if (graphImage.getScaledHeight() > document.getPageSize().getHeight() - document.topMargin() - document.bottomMargin() - document.bottom(15)) {
            document.newPage();
        }
        return graphImage;
    }

    /**
     * Formats a list of Integer IDs into a comma-separated String.
     * If the list is null or empty, it returns the string "All".
     *
     * @param ids The list of Integer IDs to format.
     * @return A comma-separated String of IDs, or "All" if the list is null or empty.
     */
    private String formatIdList(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return "All";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            sb.append(ids.get(i));
            if (i < ids.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

}
