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
@Table(name = "\"TicketInteractions\"")
public class TicketInteraction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"InteractionId\"", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "\"TicketId\"", nullable = false)
    private Tickets tickets;

    @NotNull
    @Column(name = "\"AuthorType\"", nullable = false, length = Integer.MAX_VALUE)
    private String authorType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"AuthorUserId\"")
    private User authorUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"AuthorAgentId\"")
    private Agent authorAgent;

    @NotNull
    @Column(name = "\"Message\"", nullable = false, length = Integer.MAX_VALUE)
    private String message;

    @Column(name = "\"IsPublic\"")
    private Boolean isPublic;

    @Column(name = "\"CreatedAt\"")
    private Instant createdAt;

    @Size(max = 50)
    @Column(name = "attachmentid", length = 50)
    private String attachmentid;

    @Size(max = 50)
    @Column(name = "filename", length = 50)
    private String filename;

    @Size(max = 50)
    @Column(name = "mimetype", length = 50)
    private String mimetype;

    @Size(max = 50)
    @Column(name = "sizebytes", length = 50)
    private String sizebytes;

    @Size(max = 50)
    @Column(name = "storagepath", length = 50)
    private String storagepath;

    @Size(max = 50)
    @Column(name = "uploadedat", length = 50)
    private String uploadedat;

}