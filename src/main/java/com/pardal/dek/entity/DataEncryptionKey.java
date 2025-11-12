package com.pardal.dek.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "data_encryption_key", schema = "public")
public class DataEncryptionKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name_dek")
    private String nameDek;

    @Column(name = "email_dek")
    private String emailDek;

    @Column(name = "phone_dek")
    private String phoneDek;

    @Column(name = "reference_id")
    private Integer referenceId;
}