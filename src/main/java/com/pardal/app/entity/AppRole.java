package com.pardal.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "app_role", schema = "pardal")
public class AppRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rl_id", nullable = false)
    private Integer id;

    @Size(max = 20)
    @NotNull
    @Column(name = "rl_name", nullable = false, length = 20)
    private String rlName;

}