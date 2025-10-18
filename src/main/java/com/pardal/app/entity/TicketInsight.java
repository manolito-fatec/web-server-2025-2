package com.pardal.app.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Map;

import lombok.Data;

import java.util.List;

/**
 * Represents an AI-generated insight based on a support ticket.
 * This entity is stored in the "product_insights" MongoDB collection.
 */
@Data
@Document(collection = "product_insights")
public class TicketInsight {

    @Id
    private String id;

    @Field("company_id")
    private Integer companyId;

    @Field("company_name")
    private String companyName;

    @Field("product_id")
    private Integer productId;

    @Field("product_name")
    private String productName;

    private List<InsightTheme> insights;

    private String starttime;
    private String endtime;
    private String dth;
}