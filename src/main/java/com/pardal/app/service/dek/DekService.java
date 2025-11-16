package com.pardal.app.service.dek;

import com.pardal.dek.entity.DataEncryptionKey;
import com.pardal.dek.repository.DataEncryptionKeyRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DekService {

    private final DataEncryptionKeyRepository repository;

    public DataEncryptionKey save(DataEncryptionKey dataEncryptionKey) {
        return repository.save(dataEncryptionKey);
    }

    public Optional<DataEncryptionKey> findByUserId(Integer user_id) {
        return repository.findByReferenceId(user_id);
    }

    @Transactional
    public boolean deleteByUserId(Integer user_id) {
        repository.removeDataEncryptionKeyByReferenceId(user_id);
        if(repository.findByReferenceId(user_id).isPresent()){
            log.error("Error at excluding Dek from user with ID: {}", user_id);
            throw new RuntimeException("Erro ao excluir DataEncryptionKey");
        }
        return true;
    }

    public List<DataEncryptionKey> findAll() {
        return repository.findAll();
    }

    public DataEncryptionKey updateDek(DataEncryptionKey dataEncryptionKey) {
        return repository.save(dataEncryptionKey);
    }
}
