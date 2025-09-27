package com.pardal.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "sla_plans")
public class SlaPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sla_plan_id", nullable = false)
    private Integer id;

    @Size(max = 80)
    @NotNull
    @Column(name = "name", nullable = false, length = 80)
    private String name;

    @Column(name = "first_response_mins")
    private Integer firstResponseMins;

    @Column(name = "resolution_mins")
    private Integer resolutionMins;

}