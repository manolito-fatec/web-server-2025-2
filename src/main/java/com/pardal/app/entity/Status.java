package com.pardal.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "\"Statuses\"")
public class Status {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"StatusId\"", nullable = false)
    private Integer id;

    @Size(max = 60)
    @NotNull
    @Column(name = "\"Name\"", nullable = false, length = 60)
    private String name;

}