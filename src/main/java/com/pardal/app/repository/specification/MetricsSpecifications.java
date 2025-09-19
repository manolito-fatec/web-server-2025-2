package com.pardal.app.repository.specification;

import com.pardal.app.entity.SlaPlan;
import com.pardal.app.entity.TicketStatusHistory;
import com.pardal.app.entity.Tickets;

import com.pardal.app.util.Gambiarra;
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
@Gambiarra(descricao = """
        O uso de component e métodos dinamicos deixam a specification menos amigavel de usar.
        O ideal é remover o @Component e deixar os métodos todos static para o encadeamento de métodos
        (Ex.: Specification<Tickets> ticketsDoProdutoTalDeHoje = TicketsSpecifications.hasProductId(123).and(TicketsSpecifications.hasDateAfter(LocalDateTime.now());
        """,
        autor = "Pauleta")
public class MetricsSpecifications {

    @Gambiarra(autor = "Pauleta", descricao = "Colocado dentro do MetricsSpecification para uso no front, MUDAR PARA TICKETS SPECIFICATION DEPOIS", data = "2025/09/17")
    public Specification<Tickets> hasProductId(Integer productId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("product").get("id"), productId);
    }

    @Gambiarra(autor = "Pauleta", descricao = "Colocado dentro do MetricsSpecification para uso no front, MUDAR PARA TICKETS SPECIFICATION DEPOIS", data = "2025/09/17")
    public Specification<Tickets> hasClientId(Integer clientId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("company").get("id"), clientId);
    }

    @Gambiarra(autor = "Pauleta", descricao = "Colocado dentro do MetricsSpecification para uso no front, MUDAR PARA TICKETS SPECIFICATION DEPOIS", data = "2025/09/17")
    public Specification<Tickets> hasDateAfter(LocalDateTime date) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), date);
    }

    @Gambiarra(autor = "Pauleta", descricao = "Metido dentro do MetricsSpecification para uso no front, MUDAR PARA TICKETS STATUS HISTORY SPECIFICATION DEPOIS", data = "2025/09/17")
    public Specification<Tickets> hasDateBefore(LocalDateTime date) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), date);
    }

    @Gambiarra(autor = "Pauleta", descricao = "Metido dentro do MetricsSpecification para uso no front, MUDAR PARA TICKETS STATUS HISTORY SPECIFICATION DEPOIS", data = "2025/09/17")
    public Specification<TicketStatusHistory> isReOpened() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("fromStatus").get("id"), 5);
    }

    @Gambiarra(autor = "Pauleta", descricao = "Metido dentro do MetricsSpecification para uso no front, MUDAR PARA TICKETS STATUS HISTORY SPECIFICATION DEPOIS", data = "2025/09/17")
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
 * The `TIMESTAMPDIFF` function is used to calculate the difference in minutes directly in the database.
 *
 * @return A Specification for the 'SLA met' condition.
 */
    @Gambiarra(autor = "André Wakugawa", descricao = "Colocado dentro do MetricsSpecification para uso no front, MUDAR PARA TICKETS SPECIFICATION DEPOIS", data = "2025/09/18")
    public static Specification<Tickets> isSlaMet() {
        return (root, query, cb) -> {
            Join<Tickets, SlaPlan> slaPlanJoin = root.join("slaPlan");

            Predicate closedAtIsNotNull = cb.isNotNull(root.get("closedAt"));

            var timestampDiff = cb.function(
                    "TIMESTAMPDIFF",
                    Long.class,
                    cb.literal("MINUTE"),
                    root.get("createdAt"),
                    root.get("closedAt")
            );

            Predicate resolutionTimeIsMet = cb.lessThanOrEqualTo(
                    timestampDiff,
                    slaPlanJoin.get("resolutionMins")
            );

            return cb.and(closedAtIsNotNull, resolutionTimeIsMet);
        };
    }
}
