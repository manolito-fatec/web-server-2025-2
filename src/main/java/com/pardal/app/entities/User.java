package com.pardal.app.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "\"Users\"")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"UserId\"", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"CompanyId\"")
    private Company company;

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

    @Size(max = 32)
    @Column(name = "\"CPF\"", length = 32)
    private String cpf;

    @Column(name = "\"CreatedAt\"")
    private Instant createdAt;

    @Column(name = "\"IsVIP\"")
    private Boolean isVIP;

}