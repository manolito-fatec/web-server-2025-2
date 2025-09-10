package com.pardal.app.service.Tickets;

import com.pardal.app.entity.Tickets;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class TicketsSpecifications {

    public static Specification<Tickets> hasProductId(Integer productId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("productId"), productId);
    }

    public static Specification<Tickets> hasClientId(Integer clientId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("clientId"), clientId);
    }

    public static Specification<Tickets> hasDateAfter(LocalDateTime date) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("creationDate"), date);
    }

    public static Specification<Tickets> hasDateBefore(LocalDateTime date) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get("creationDate"), date);
    }
}