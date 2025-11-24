package com.pardal.app.repository.specification.tickets;

import com.pardal.app.entity.Product;
import com.pardal.app.entity.SlaPlan;
import com.pardal.app.entity.TicketStatusHistory;
import com.pardal.app.entity.Tickets;
import com.pardal.app.entity.dto.insights.InsightsFilterDto;

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

    private static final String PRODUCT = "product";
    private static final String PRODUCT_NAME = "productName";
    private static final String PRODUCT_ID = "productId";
    private static final String COMPANY = "company";
    private static final String ID = "id";
    private static final String CREATED_AT = "createdAt";
    private static final String CLOSED_AT = "closedAt";
    private static final String TICKETS = "tickets";
    private static final String TOTAL_TICKETS = "totalTickets";
    private static final String FROM_STATUS = "fromStatus";
    private static final String SLA_PLAN = "slaPlan";
    private static final String RESOLUTION_MINS = "resolutionMins";
    private static final String NAME = "name";
    private static final Integer RE_OPENED_STATUS = 5;

     private TicketsSpecification() {
        throw new UnsupportedOperationException("Utility class");
    }


    public static Specification<Tickets> withDateRangeAndFilters(TicketsFilters pFilters) {

        return filterOptionalParams(pFilters);
    }

    public static Specification<Tickets> withDateRangeAndFilterList(InsightsFilterDto pFilters) {

        return filterOptionalListParams(pFilters.getCustomerIds(), pFilters.getProductIds());
    }
    
    private static Specification<Tickets> filterOptionalParams(TicketsFilters pFilters) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (pFilters.getProductId() != null) {
                predicates.add(criteriaBuilder.equal(root.get(PRODUCT).get(ID), pFilters.getProductId()));
            }

            if (pFilters.getCustomerId() != null) {
                predicates.add(criteriaBuilder.equal(root.get(COMPANY).get(ID), pFilters.getCustomerId()));
            }

            if (pFilters.getFromDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get(CREATED_AT), pFilters.getFromDate().toInstant(ZoneOffset.UTC)));
            }

            if (pFilters.getToDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get(CREATED_AT), pFilters.getToDate().toInstant(ZoneOffset.UTC)));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static Specification<Tickets> filterOptionalListParams(List<Integer> customerIdList, List<Integer> productIdList) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (productIdList != null && !productIdList.isEmpty()) {
                predicates.add(root.get(PRODUCT).get(ID).in(productIdList));
            }

            if (customerIdList != null && !customerIdList.isEmpty()) {

                predicates.add(root.get(COMPANY).get(ID).in(customerIdList));
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
            Join<TicketStatusHistory, Tickets> ticketJoin = root.join(TICKETS);
            List<Predicate> predicates = new ArrayList<>();

            if (pProductId.isPresent())
            {
                predicates.add(criteriaBuilder.equal(ticketJoin.get(PRODUCT).get(ID), pProductId.get()));
            }

            if (pCustomerId.isPresent()) {
                predicates.add(criteriaBuilder.equal(ticketJoin.get(COMPANY).get(ID), pCustomerId.get()));
            }

            if (pFromDate.isPresent())
            {
                pFromDate.get().toInstant(ZoneOffset.UTC);
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(ticketJoin.get(CREATED_AT), pFromDate.get().toInstant(ZoneOffset.UTC)));
            }

            if (pToDate.isPresent())
            {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(ticketJoin.get(CREATED_AT), pToDate.get().toInstant(ZoneOffset.UTC)));
            }

            query.distinct(true);
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<TicketStatusHistory> isReOpened() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get(FROM_STATUS).get(ID), RE_OPENED_STATUS);
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
            Join<Tickets, SlaPlan> slaPlanJoin = root.join(SLA_PLAN);

            Predicate closedAtIsNotNull = cb.isNotNull(root.get(CLOSED_AT));

            HibernateCriteriaBuilder hcb = (HibernateCriteriaBuilder) cb;
            var timestampDiff = hcb.durationByUnit(
                    TemporalUnit.MINUTE,
                    hcb.durationBetween(root.get(CLOSED_AT),root.get(CREATED_AT))
            );
            Predicate resolutionTimeIsMet = cb.lessThanOrEqualTo(
                    timestampDiff,
                    slaPlanJoin.get(RESOLUTION_MINS)
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

            Join<Tickets, Product> product = root.join(PRODUCT);

            query.multiselect(
                    product.get(ID).alias(PRODUCT_ID),
                    product.get(NAME).alias(PRODUCT_NAME),
                    cb.count(root.get(ID)).alias(TOTAL_TICKETS)
            );

            query.groupBy(product.get(ID), product.get(NAME));
            query.orderBy(cb.asc(product.get(NAME)));

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
            criteriaBuilder.isNotNull(root.get(CLOSED_AT));
    }

}
