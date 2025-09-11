package com.pardal.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "\"AuditLogs\"")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"AuditId\"", nullable = false)
    private Long id;

    @Size(max = 50)
    @NotNull
    @Column(name = "\"EntityType\"", nullable = false, length = 50)
    private String entityType;

    @NotNull
    @Column(name = "\"EntityId\"", nullable = false)
    private Long entityId;

    @Size(max = 20)
    @NotNull
    @Column(name = "\"Operation\"", nullable = false, length = 20)
    private String operation;

    @Size(max = 120)
    @Column(name = "\"PerformedBy\"", length = 120)
    private String performedBy;

    @Column(name = "\"PerformedAt\"")
    private Instant performedAt;

    @Column(name = "\"DetailsJson\"", length = Integer.MAX_VALUE)
    private String detailsJson;

}