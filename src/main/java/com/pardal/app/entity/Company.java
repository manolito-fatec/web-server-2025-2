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
@Table(name = "\"Companies\"")
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"CompanyId\"", nullable = false)
    private Integer id;

    @Size(max = 120)
    @NotNull
    @Column(name = "\"Name\"", nullable = false, length = 120)
    private String name;

    @Size(max = 32)
    @Column(name = "\"CNPJ\"", length = 32)
    private String cnpj;

    @Size(max = 60)
    @Column(name = "\"Segmento\"", length = 60)
    private String segmento;

    @Column(name = "\"CreatedAt\"")
    private Instant createdAt;

}