package com.pardal.app.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "\"TicketTags\"")
public class TicketTag {
    @EmbeddedId
    private TicketTagId id;

    @MapsId("ticketId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "\"TicketId\"", nullable = false)
    private Ticket ticket;

    @MapsId("tagId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "\"TagId\"", nullable = false)
    private Tag tag;

}