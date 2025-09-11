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
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

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

    @Size(max = 32)
    @Column(name = "cpf", length = 32)
    private String cpf;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "is_vip")
    private Boolean isVip;

}