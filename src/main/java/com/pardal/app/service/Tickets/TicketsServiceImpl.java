package com.pardal.app.service.Tickets;

import com.pardal.app.entity.Tickets;
import com.pardal.app.repository.TicketRepository;
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

    @Transactional
    @Override
    public Integer getTicketsCount(Optional<Integer> productId,
                                   Optional<Integer> clientId,
                                   Optional<LocalDateTime> dateMin,
                                   Optional<LocalDateTime> dateMax) {

        Specification<Tickets> spec = Specification.where(null);

        if (productId.isPresent()) {
            spec = spec.and(TicketSpecifications.hasProductId(productId.get()));
        }
        if (clientId.isPresent()) {
            spec = spec.and(TicketSpecifications.hasClientId(clientId.get()));
        }
        if (dateMin.isPresent()) {
            spec = spec.and(TicketSpecifications.hasDateAfter(dateMin.get()));
        }
        if (dateMax.isPresent()) {
            spec = spec.and(TicketSpecifications.hasDateBefore(dateMax.get()));
        }

        long count = ticketsRepository.count(spec);

        return (int) count;
    }
}
