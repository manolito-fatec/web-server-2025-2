package com.pardal.app.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "\"Priorities\"")
public class Priority {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"PriorityId\"", nullable = false)
    private Integer id;

    @Size(max = 50)
    @NotNull
    @Column(name = "\"Name\"", nullable = false, length = 50)
    private String name;

    @Column(name = "\"Weight\"")
    private Integer weight;

}