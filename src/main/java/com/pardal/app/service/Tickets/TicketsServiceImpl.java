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

        productId.ifPresent(pId -> spec.and(TicketsSpecifications.hasProductId(pId)));
        clientId.ifPresent(cId -> spec.and(TicketsSpecifications.hasClientId(cId)));
        dateMin.ifPresent(dMin -> spec.and(TicketsSpecifications.hasDateAfter(dMin)));
        dateMax.ifPresent(dMax -> spec.and(TicketsSpecifications.hasDateBefore(dMax)));

        long count = ticketsRepository.count(spec);

        return (int) count;
    }
}
