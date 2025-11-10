package com.pardal.app.entity.log;

import java.time.LocalDateTime;

import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "application_logs")
public class LogEntry
{
    @Id
    private String id;
    private String level;
    private String logger;
    private String message;
    private String title;
    private LocalDateTime timestamp;
    private String thread;
    private String exception;
    private String userEmail;
    private String remoteIp;
    private String httpMethod;
    private String requestURI;
}
