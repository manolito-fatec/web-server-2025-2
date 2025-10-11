package com.pardal.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "app_users", schema = "pardal")
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usr_id", nullable = false)
    private Integer id;

    @Size(max = 255)
    @NotNull
    @Column(name = "usr_name", nullable = false)
    private String name;

    @Size(max = 255)
    @NotNull
    @Column(name = "usr_email", nullable = false)
    private String email;

    @Size(max = 15)
    @NotNull
    @Column(name = "usr_phone", nullable = false, length = 15)
    private String phone;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ColumnDefault("1")
    @JoinColumn(name = "rl_id", nullable = false)
    private AppRole role_id;

    @Column(name = "usr_expire_date")
    private LocalDate expireDate;

    @Size(max = 255)
    @Column(name = "usr_pwd")
    private String password;

}