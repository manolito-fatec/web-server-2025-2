package com.pardal.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "agents")
public class Agent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "agent_id", nullable = false)
    private Integer id;

    @Size(max = 120)
    @NotNull
    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Size(max = 254)
    @Column(name = "email", length = 254)
    private String email;

    @Size(max = 40)
    @Column(name = "phone", length = 40)
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "hired_at")
    private LocalDate hiredAt;

}