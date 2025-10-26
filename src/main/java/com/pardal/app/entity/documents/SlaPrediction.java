package com.pardal.app.entity.documents;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "sla_predictions")
public record SlaPrediction(
        @Id String id,

        @Field("ticket_id")
        Integer ticketId,

        @Field("company_id")
        Integer companyId,

        @Field("company_name")
        String companyName,

        @Field("product_id")
        Integer productId,

        @Field("product_name")
        String productName,

        @Field("subcategory_id")
        Integer subcategoryId,

        @Field("subcategory_name")
        String subcategoryName,

        @Field("sla_breach_probability")
        Double slaBreachProbability,

        String dth
) {}