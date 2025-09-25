package com.pardal.app.repository.specification;

import com.pardal.app.entity.Tickets;
import com.pardal.app.entity.dto.DashboardFilterDto;
import jakarta.persistence.criteria.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class TicketsSpecification {

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

            if (pFilters.getStartDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("createdAt"), pFilters.getStartDate().toInstant(ZoneOffset.UTC)));
            }

            if (pFilters.getEndDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("createdAt"), pFilters.getEndDate().toInstant(ZoneOffset.UTC)));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
