package com.pardal.app.service.export;

import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

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

        try (CSVWriter csvWriter = new CSVWriter(
                writer,
                CSVWriter.DEFAULT_SEPARATOR,
                CSVWriter.DEFAULT_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END)) {
            StatefulBeanToCsv<T> beanToCsv = new StatefulBeanToCsvBuilder<T>(csvWriter).build();
            beanToCsv.write(dataList);
        } catch (CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
            throw new RuntimeException(e);
        }
    }

}
