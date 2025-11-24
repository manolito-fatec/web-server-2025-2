package com.pardal.dek.repository;

import com.pardal.dek.entity.DataEncryptionKey;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DataEncryptionKeyRepository extends JpaRepository<DataEncryptionKey, Long> {

    Optional<DataEncryptionKey> findByReferenceId(Integer referenceId);

    @Transactional
    void removeDataEncryptionKeyByReferenceId(Integer referenceId);
}
