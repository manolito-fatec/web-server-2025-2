package com.pardal.app.repository.specification;

import com.pardal.app.entity.TicketStatusHistory;
import com.pardal.app.entity.Tickets;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class MetricsSpecifications {

    public Specification<Tickets> hasProductId(Integer productId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("product").get("id"), productId);
    }

    public Specification<Tickets> hasClientId(Integer clientId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("company").get("id"), clientId);
    }

    public Specification<Tickets> hasDateAfter(LocalDateTime date) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), date);
    }

    public Specification<Tickets> hasDateBefore(LocalDateTime date) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), date);
    }

    public Specification<TicketStatusHistory> isReOpened() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("fromStatus").get("id"), 5);
    }

    public Specification<TicketStatusHistory> joinWithTicket(
            Optional<Integer> pProductId,
            Optional<Integer> pCustomerId,
            Optional<LocalDateTime> pFromDate,
            Optional<LocalDateTime> pToDate) {

        return (root, query, criteriaBuilder) -> {
            Join<TicketStatusHistory, Tickets> ticketJoin = root.join("tickets");
            List<Predicate> predicates = new ArrayList<>();

            if (pProductId.isPresent())
            {
                predicates.add(criteriaBuilder.equal(ticketJoin.get("product").get("id"), pProductId.get()));
            }

            if (pCustomerId.isPresent()) {
                predicates.add(criteriaBuilder.equal(ticketJoin.get("company").get("id"), pCustomerId.get()));
            }

            if (pFromDate.isPresent())
            {
                pFromDate.get().toInstant(ZoneOffset.UTC);
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(ticketJoin.get("createdAt"), pFromDate.get().toInstant(ZoneOffset.UTC)));
            }

            if (pToDate.isPresent())
            {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(ticketJoin.get("createdAt"), pToDate.get().toInstant(ZoneOffset.UTC)));
            }

            query.distinct(true);
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}