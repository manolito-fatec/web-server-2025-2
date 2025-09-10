package com.pardal.app.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "\"Agents\"")
public class Agent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"AgentId\"", nullable = false)
    private Integer id;

    @Size(max = 120)
    @NotNull
    @Column(name = "\"FullName\"", nullable = false, length = 120)
    private String fullName;

    @Size(max = 254)
    @Column(name = "\"Email\"", length = 254)
    private String email;

    @Size(max = 40)
    @Column(name = "\"Phone\"", length = 40)
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"DepartmentId\"")
    private Department department;

    @Column(name = "\"IsActive\"")
    private Boolean isActive;

    @Column(name = "\"HiredAt\"")
    private LocalDate hiredAt;

}