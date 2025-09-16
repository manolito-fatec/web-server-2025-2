package com.pardal.app.repository.specifications;

import com.pardal.app.entity.Tickets;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class TicketSpecifications {

    public static Specification<Tickets> hasProductId(Integer productId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("product").get("id"), productId);
    }

    public static Specification<Tickets> hasClientId(Integer clientId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("company").get("id"), clientId);
    }

    public static Specification<Tickets> isCreatedAfter(LocalDateTime date) {
        // The entity field `createdAt` is an Instant, so we convert the LocalDateTime
        Instant instant = date.toInstant(ZoneOffset.UTC);
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), instant);
    }

    public static Specification<Tickets> isCreatedBefore(LocalDateTime date) {
        // The entity field `createdAt` is an Instant, so we convert the LocalDateTime
        Instant instant = date.toInstant(ZoneOffset.UTC);
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), instant);
    }
}