package com.pardal.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "\"Tickets\"")
public class Tickets {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"TicketId\"", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "\"CompanyId\"", nullable = false)
    private Company company;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "\"CreatedByUserId\"", nullable = false)
    private User createdByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"AssignedAgentId\"")
    private Agent assignedAgent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"ProductId\"")
    private Product product;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "\"CategoryId\"", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"SubcategoryId\"")
    private Subcategory subcategory;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "\"PriorityId\"", nullable = false)
    private Priority priority;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "\"CurrentStatusId\"", nullable = false)
    private Status currentStatus;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "\"SLAPlanId\"", nullable = false)
    private SlaPlan sLAPlan;

    @Size(max = 200)
    @NotNull
    @Column(name = "\"Title\"", nullable = false, length = 200)
    private String title;

    @Column(name = "\"Description\"", length = Integer.MAX_VALUE)
    private String description;

    @Size(max = 40)
    @Column(name = "\"Channel\"", length = 40)
    private String channel;

    @Size(max = 60)
    @Column(name = "\"Device\"", length = 60)
    private String device;

    @Column(name = "\"CreatedAt\"")
    private LocalDateTime createdAt;

    @Column(name = "\"FirstResponseAt\"")
    private LocalDateTime firstResponseAt;

    @Column(name = "\"ClosedAt\"")
    private LocalDateTime closedAt;

}