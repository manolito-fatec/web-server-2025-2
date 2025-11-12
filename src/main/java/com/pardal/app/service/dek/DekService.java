package com.pardal.app.service.dek;

import com.pardal.dek.entity.DataEncryptionKey;
import com.pardal.dek.repository.DataEncryptionKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DekService {

    private final DataEncryptionKeyRepository repository;

    public DataEncryptionKey save(DataEncryptionKey dataEncryptionKey) {
        return repository.save(dataEncryptionKey);
    }

    public Optional<DataEncryptionKey> findByUserId(Integer user_id) {
        return repository.findByReferenceId(user_id);
    }
}
