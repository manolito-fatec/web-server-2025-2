package com.pardal.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "\"SLA_Plans\"")
public class SlaPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"SLAPlanId\"", nullable = false)
    private Integer id;

    @Size(max = 80)
    @NotNull
    @Column(name = "\"Name\"", nullable = false, length = 80)
    private String name;

    @Column(name = "\"FirstResponseMins\"")
    private Integer firstResponseMins;

    @Column(name = "\"ResolutionMins\"")
    private Integer resolutionMins;

}