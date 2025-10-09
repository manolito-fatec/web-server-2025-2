package com.pardal.app.repository.specification;

import com.pardal.app.entity.Product;
import com.pardal.app.entity.SlaPlan;
import com.pardal.app.entity.TicketStatusHistory;
import com.pardal.app.entity.Tickets;
import com.pardal.app.entity.dto.DashboardFilterDto;

import jakarta.persistence.criteria.*;

import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.sqm.TemporalUnit;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TicketsSpecification {

    public static Specification<Tickets> withDateRangeAndFilters(DashboardFilterDto pFilters) {

        return filterOptionalParams(pFilters);
    }

    private static Specification<Tickets> filterOptionalParams(DashboardFilterDto pFilters) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (pFilters.getProductId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("product").get("id"), pFilters.getProductId()));
            }

            if (pFilters.getCustomerId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("company").get("id"), pFilters.getCustomerId()));
            }

            if (pFilters.getFromDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("createdAt"), pFilters.getFromDate().toInstant(ZoneOffset.UTC)));
            }

            if (pFilters.getToDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("createdAt"), pFilters.getToDate().toInstant(ZoneOffset.UTC)));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

     public static Specification<TicketStatusHistory> joinWithTicketStatusHistory(
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



    public static Specification<TicketStatusHistory> isReOpened() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("fromStatus").get("id"), 5);
    }

    /**
     * Generates a Specification that filters tickets which have met the resolution SLA.
     * <p>
     * The condition checks if:
     * <p>
     * 1. The ticket has a closing date (`closedAt`).
     * <p>
     * 2. The time between the creation (`createdAt`) and the closing (`closedAt`) is less than or equal
     * to the resolution time defined in the SLA plan (`resolutionMins`).
     * <p>
     *
     * @author André Wakugawa 
     * @return A Specification for the 'SLA met' condition.
     */
    public static Specification<Tickets> isSlaMet() {
        return (root, query, cb) -> {
            Join<Tickets, SlaPlan> slaPlanJoin = root.join("slaPlan");

            Predicate closedAtIsNotNull = cb.isNotNull(root.get("closedAt"));

            HibernateCriteriaBuilder hcb = (HibernateCriteriaBuilder) cb;
            var timestampDiff = hcb.durationByUnit(
                    TemporalUnit.MINUTE,
                    hcb.durationBetween(root.get("closedAt"),root.get("createdAt"))
            );
            Predicate resolutionTimeIsMet = cb.lessThanOrEqualTo(
                    timestampDiff,
                    slaPlanJoin.get("resolutionMins")
            );

            return cb.and(closedAtIsNotNull, resolutionTimeIsMet);
        };
    }

   /**
    * Selects the product ID, product name, and the total count of tickets per product.
    *
    * @author Paulo Arantes 
    * @return a specification projecting product information with the number of tickets associated.
    */
    public static Specification<Tickets> findTicketsByProduct() {
        return (root, query, cb) -> {

            Join<Tickets, Product> product = root.join("product");

            query.multiselect(
                    product.get("id").alias("productId"),
                    product.get("name").alias("productName"),
                    cb.count(root.get("id")).alias("totalTickets")
            );

            query.groupBy(product.get("id"), product.get("name"));
            query.orderBy(cb.asc(product.get("name")));

            return cb.conjunction();
        };
    }

    /**
     * Generates a Specification to filter tickets that have a defined closing date.
     * <p>
     * The condition checks if the `closedAt` field of a ticket is not null.
     * <p>
     * This is useful for retrieving all tickets that have been closed, regardless of any other criteria.
     *
     * @author Gabriel Bartolomeu
     * @return A Specification for the 'is closed' condition.
     */
    public static Specification<Tickets> isClosed() {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.isNotNull(root.get("closedAt"));
    }

}
