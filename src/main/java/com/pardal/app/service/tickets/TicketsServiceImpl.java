package com.pardal.app.service.tickets;

import com.pardal.app.entity.Tickets;
import com.pardal.app.repository.TicketRepository;
import com.pardal.app.repository.specification.MetricsSpecifications;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TicketsServiceImpl implements TicketsService {

    @Autowired
    private TicketRepository ticketsRepository;

    @Autowired
    private MetricsSpecifications metricsSpecifications;

    @Override
    public long getTicketsCount(Optional<Integer> productId,
                                   Optional<Integer> clientId,
                                   Optional<LocalDateTime> dateMin,
                                   Optional<LocalDateTime> dateMax) {

        Specification<Tickets> spec = Specification.where(null);

        if (productId.isPresent()) {
            spec = spec.and(metricsSpecifications.hasProductId(productId.get()));
        }
        if (clientId.isPresent()) {
            spec = spec.and(metricsSpecifications.hasClientId(clientId.get()));
        }
        if (dateMin.isPresent()) {
            spec = spec.and(metricsSpecifications.hasDateAfter(dateMin.get()));
        }
        if (dateMax.isPresent()) {
            spec = spec.and(metricsSpecifications.hasDateBefore(dateMax.get()));
        }

        return ticketsRepository.count(spec);
    }
}
