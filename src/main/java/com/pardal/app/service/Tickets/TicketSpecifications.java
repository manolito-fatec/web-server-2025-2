package com.pardal.app.service.Tickets;

import com.pardal.app.entity.Tickets;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDateTime;

public class TicketSpecifications {

    public static Specification<Tickets> hasProductId(Integer productId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("priority").get("id"), productId);
    }

    public static Specification<Tickets> hasClientId(Integer clientId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("company").get("id"), clientId);
    }

    public static Specification<Tickets> hasDateAfter(LocalDateTime date) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), date);
    }

    public static Specification<Tickets> hasDateBefore(LocalDateTime date) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get("closedAt"), date);
    }
}