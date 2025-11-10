package com.pardal.app.service.export;

import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;
import com.pardal.app.entity.dto.insights.InsightsPdfRequestDto;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

@Service
public class PdfExportService {

    /**
     * Generates a PDF containing the captured graph images and returns it as a byte array.
     * @param request The DTO containing filters and Base64 encoded images.
     * @return The generated PDF file content as a byte array.
     * @throws IOException If image decoding or PDF writing fails.
     */
    public byte[] generateInsightsPdf(InsightsPdfRequestDto request) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Document document = new Document();

            PdfWriter.getInstance(document, bos);

            document.open();

            setupTitlesToDocument(request, document);
            addFiltersToDocument(request, document);
            addImagesToDocument(request, document);

            document.close();

            return bos.toByteArray();
        } catch (Exception e) {
            System.err.println("Critical error during PDF generation: " + e.getMessage());
            throw new RuntimeException("Failed to generate PDF document.", e);
        }
    }

    private void setupTitlesToDocument(InsightsPdfRequestDto request, Document document) throws DocumentException {
        Font filterFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.BLACK);

        document.add(new Paragraph("Applied Filters:", filterFont));

        String customerIdsStr = formatIdList(request.getCustomerIds());
        document.add(new Paragraph("  Customer IDs: " + customerIdsStr, filterFont));

        String productIdsStr = formatIdList(request.getProductIds());
        document.add(new Paragraph("  Product IDs: " + productIdsStr, filterFont));

        document.add(new Paragraph("\n"));
    }

    private void addImagesToDocument(InsightsPdfRequestDto request, Document document) throws DocumentException {
        List<String> base64Images = request.getGraphImagesBase64();
        if (base64Images != null) {
            for (String base64Image : base64Images) {
                try {
                    Image graphImage = processBase64Image(base64Image, document);

                    document.add(graphImage);
                    document.add(new Paragraph("\n\n"));
                } catch (Exception e) {
                    System.err.println("Error adding image to PDF: " + e.getMessage());
                    document.add(new Paragraph(
                            "[Image failed to load: " + e.getMessage() + "]",
                            new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC, BaseColor.RED)));
                }
            }
        }
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

    // formata lista em string separado por virgula. All pra tudo e vazio se null.
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
