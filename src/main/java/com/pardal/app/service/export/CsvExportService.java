package com.pardal.app.service.export;

import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.pardal.app.entity.documents.Forecaster;
import com.pardal.app.entity.documents.TicketInsight;
import com.pardal.app.entity.dto.insights.InsightsDataDto;
import com.pardal.app.entity.dto.insights.SlaPredictionResponseDto;
import com.pardal.app.entity.dto.metrics.TicketsBySubcategoryCountDto;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class CsvExportService {

    /**
     * Writes a list of objects to a PrintWriter in CSV format.
     * The CSV headers are derived from the field names of the object class.
     *
     * @param <T> The type of objects in the list.
     * @param writer The PrintWriter to write the CSV data to.
     * @param dataList The list of data objects to export.
     * @param type The Class object of the data type (e.g. TicketInsight.class).
     */
    public <T> void writeCsv(PrintWriter writer, List<T> dataList, Class<T> type) throws IOException {

        CSVWriter csvWriter = new CSVWriter(writer,
                CSVWriter.DEFAULT_SEPARATOR,
                CSVWriter.DEFAULT_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END);

        try {
            StatefulBeanToCsv<T> beanToCsv = new StatefulBeanToCsvBuilder<T>(csvWriter).build();
            beanToCsv.write(dataList);
            csvWriter.flush();
        } catch (CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
            throw new RuntimeException("CSV Generation Error", e);
        }
    }

    /**
     * Centralizes the ZIP creation and CSV writing for all insights data.
     * @param data The InsightsDataDto containing all lists.
     * @param outputStream The output stream from the HttpServletResponse.
     * @throws IOException if an error occurs during ZIP or CSV writing.
     */
    public void exportInsightsZip(InsightsDataDto data, OutputStream outputStream) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(outputStream)) {
            addCsvToZip(zos, "sla_prediction_data.csv", data.getSlaInsightData(), SlaPredictionResponseDto.class);
            addCsvToZip(zos, "seasonality_forecaster_data.csv", data.getSeasonalityInsightData(), Forecaster.class);
            addCsvToZip(zos, "product_insights_data.csv", data.getProductInsightsData(), TicketInsight.class);
            addCsvToZip(zos, "pareto_subcategory_data.csv", data.getParetoInsightData(), TicketsBySubcategoryCountDto.class);
        } catch (RuntimeException e) {
            throw new IOException("Failed to generate CSV inside ZIP file.", e);
        }
    }

    private <T> void addCsvToZip(ZipOutputStream zos, String entryName, List<T> dataList, Class<T> type) throws IOException {
        if (dataList == null || dataList.isEmpty()) {
            return;
        }

        ZipEntry entry = new ZipEntry(entryName);
        zos.putNextEntry(entry);

        PrintWriter writer = new PrintWriter(new OutputStreamWriter(zos, StandardCharsets.UTF_8));

        this.writeCsv(writer, dataList, type);
        writer.flush();

        zos.closeEntry();
    }

}
