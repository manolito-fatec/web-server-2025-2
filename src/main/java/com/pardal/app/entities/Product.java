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
@Table(name = "\"Products\"")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"ProductId\"", nullable = false)
    private Integer id;

    @Size(max = 150)
    @NotNull
    @Column(name = "\"Name\"", nullable = false, length = 150)
    private String name;

    @Size(max = 50)
    @Column(name = "\"Code\"", length = 50)
    private String code;

    @Size(max = 500)
    @Column(name = "\"Description\"", length = 500)
    private String description;

    @Column(name = "\"IsActive\"")
    private Boolean isActive;

    @Column(name = "\"CreatedAt\"")
    private Instant createdAt;

}