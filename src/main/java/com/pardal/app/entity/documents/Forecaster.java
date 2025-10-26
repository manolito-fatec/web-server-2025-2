package com.pardal.app.entity.documents;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Document(collection = "tickets_forecaster")
public class Forecaster {
    @Id
    private String id;

    private Integer productId;
    private String productName;
    private Integer companyId;
    private String companyName;
    private Integer totalTickets;
    private String futureDate;
    private String dth;
}
