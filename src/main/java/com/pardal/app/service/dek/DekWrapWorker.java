package com.pardal.app.service.dek;

import com.pardal.app.service.vault.VaultEncryptionService;
import com.pardal.dek.entity.DataEncryptionKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DekWrapWorker {

    private final DekService dekService;
    private final VaultEncryptionService vaultEncryptionService;

    @Scheduled(fixedDelay = 2592000000L)
    public void rewrapKeys(){
        log.info("Started DEK rewrap keys");
        List<DataEncryptionKey> allDeks = dekService.findAll();

        for (DataEncryptionKey dekEntry : allDeks) {
            try {
                SecretKey plaintextNameDek = vaultEncryptionService.decryptDEKWithVault(dekEntry.getNameDek());
                String newEncryptedNameDek = vaultEncryptionService.encryptDEKWithVault(plaintextNameDek);
                dekEntry.setNameDek(newEncryptedNameDek);

                SecretKey plaintextEmailDek = vaultEncryptionService.decryptDEKWithVault(dekEntry.getEmailDek());
                String newEncryptedEmailDek = vaultEncryptionService.encryptDEKWithVault(plaintextEmailDek);
                dekEntry.setEmailDek(newEncryptedEmailDek);

                SecretKey plaintextPhoneDek = vaultEncryptionService.decryptDEKWithVault(dekEntry.getPhoneDek());
                String newEncryptedPhoneDek = vaultEncryptionService.encryptDEKWithVault(plaintextPhoneDek);
                dekEntry.setPhoneDek(newEncryptedPhoneDek);

                dekService.updateDek(dekEntry);

            } catch (Exception e) {
                log.error("Error on re-wrap : {}", dekEntry.getReferenceId(), e);
            }
        }

        log.info("Re-wrap finished!.");
    }
}
