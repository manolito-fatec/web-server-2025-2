package com.pardal.app.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;

import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;

@Slf4j
public class RequestExceptionHandler
{
    private static final String TITLE = "title";
    private  static final String SERVER_ERROR = "Internal Server Error: ";

    @FunctionalInterface
    public interface ExportTask  {
        void execute() throws Exception;
    }

    @FunctionalInterface
    public interface FileGenerationTask extends Callable<byte[]> {}

    /**
     * Centralized exception handler for controller methods that return a ResponseEntity.
     *
     * It executes the provided task, automatically handling common exceptions 
     * (NoSuchElementException for 404, IllegalArgumentException for 400) 
     * and generic exceptions for 500 status codes. It also manages the MDC title.
     *
     * @param mdcTitle The title of the operation for MDC logging.
     * @param callable The controller logic encapsulated in a Callable that returns a ResponseEntity.
     * @author paulo arantes
     * @return A ResponseEntity with the successful result or an appropriate error status and body.
     */
    public static <T> ResponseEntity<?> handleRequest(String mdcTitle, Callable<ResponseEntity<T>> callable) {
        MDC.put(TITLE, mdcTitle);
        try {
            return callable.call();
        }catch (NoSuchElementException noSuchElementException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException runtimeException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SERVER_ERROR + runtimeException.getMessage());
        }catch (Exception e) {
                log.error("Erro interno do servidor durante a operação: {}", mdcTitle, e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(SERVER_ERROR + e.getMessage());
        }finally {
            MDC.remove(TITLE);
        }
    }

    /**
     * Centralized exception handler for methods that export data directly 
     * to the HttpServletResponse OutputStream (e.g., ZIP or CSV downloads).
     *
     * It executes the export task, flushes the buffer upon success, and sends 
     * an HTTP 500 error status if any exception occurs during the process. 
     * It also manages the MDC title.
     *
     * @param mdcTitle The title of the operation for MDC logging.
     * @param response The HttpServletResponse used to write the exported file.
     * @param task The logic responsible for writing the file to the output stream.
     * @author paulo arantes
     * @throws IOException If the response cannot send the error or flush the buffer.
     */
    public static void handleExport( String mdcTitle, HttpServletResponse response, ExportTask task) throws IOException 
    {
        MDC.put("title", mdcTitle); 
        try {
            task.execute();
            response.flushBuffer();
        } catch (Exception e) {
            response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error generating export file: " + e.getMessage());
        } finally
        {
            MDC.remove("title");
        }
    }

    /**
     * Centralized exception handler for controller methods that generate and 
     * return a file as a byte array (e.g., PDF).
     *
     * It executes the file generation task, configures all necessary HTTP 
     * headers for file download (Content-Type, Content-Disposition, Content-Length), 
     * and handles exceptions by returning an appropriate HTTP 500 status with an error message in the body.
     *
     * @param mdcTitle The title of the operation for MDC logging.
     * @param filename The base name of the file; a timestamp and .pdf extension are added automatically.
     * @param task The logic responsible for generating the file content as a byte array.
     * @author paulo arantes
     * @return A ResponseEntity containing the PDF byte array and download headers, or an error response.
     */
    public static ResponseEntity<byte[]> handlePdfFileRequest(String mdcTitle, String filename, FileGenerationTask task) {
        MDC.put(TITLE, mdcTitle);
        try {
            byte[] pdfBytes = task.call();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);

            String finalFilename = filename + "_" + System.currentTimeMillis() + ".pdf";

            headers.setContentDispositionFormData("attachment", finalFilename);
            headers.setContentLength(pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (IOException ioException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("IO Error: " + ioException.getMessage()).getBytes(StandardCharsets.UTF_8));
        } catch (Exception runtimeException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Internal Server Error: " + runtimeException.getMessage()).getBytes(StandardCharsets.UTF_8));
        }finally {
            MDC.remove("title");
        }
    }
}
