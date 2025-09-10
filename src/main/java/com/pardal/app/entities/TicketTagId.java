package com.pardal.app.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class TicketTagId implements Serializable {
    private static final long serialVersionUID = 5195972998153983235L;
    @NotNull
    @Column(name = "\"TicketId\"", nullable = false)
    private Long ticketId;

    @NotNull
    @Column(name = "\"TagId\"", nullable = false)
    private Integer tagId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        TicketTagId entity = (TicketTagId) o;
        return Objects.equals(this.tagId, entity.tagId) &&
                Objects.equals(this.ticketId, entity.ticketId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tagId, ticketId);
    }

}