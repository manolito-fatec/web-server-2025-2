package com.pardal.app.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "\"Categories\"")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"CategoryId\"", nullable = false)
    private Integer id;

    @Size(max = 100)
    @NotNull
    @Column(name = "\"Name\"", nullable = false, length = 100)
    private String name;

}