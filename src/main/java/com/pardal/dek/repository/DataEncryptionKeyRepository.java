package com.pardal.dek.repository;

import com.pardal.dek.entity.DataEncryptionKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DataEncryptionKeyRepository extends JpaRepository<DataEncryptionKey, Long> {

    DataEncryptionKey findByReferenceId(String referenceId);
}
