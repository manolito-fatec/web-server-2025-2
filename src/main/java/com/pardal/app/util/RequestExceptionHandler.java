package com.pardal.app.util;

import java.util.NoSuchElementException;
import java.util.concurrent.Callable;

import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RequestExceptionHandler
{
    private static final String TITLE = "title";
    private  static final String SERVER_ERROR = "Internal Server Error: ";

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
}
