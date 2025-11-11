package com.pardal.dek.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "data_encryption_key", schema = "voucher")
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
    private String referenceId;
}