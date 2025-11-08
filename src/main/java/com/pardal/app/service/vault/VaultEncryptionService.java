package com.pardal.app.service.vault;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class VaultEncryptionService {

    @Value("${spring.cloud.vault.uri}")
    private String vaultUri;

    @Value("${spring.cloud.vault.token}")
    private String vaultToken;

    private static final String TRANSIT_ENCRYPT_PATH = "/v1/transit/encrypt/user-encryption-key";
    private static final String TRANSIT_DECRYPT_PATH = "/v1/transit/decrypt/user-encryption-key";
    private static final String ALGORITHM = "AES";

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Criptografa dados usando Envelope Encryption (KEK + DEK)
     *
     * @param plaintext Texto a ser criptografado
     * @return Objeto contendo DEK criptografada e dados criptografados
     */
    public EncryptedData encryptWithEnvelope(String plaintext) {
        try {
            SecretKey dek = generateDEK();

            String encryptedData = encryptWithDEK(plaintext, dek);

            String encryptedDEK = encryptDEKWithVault(dek);

            return new EncryptedData(encryptedData, encryptedDEK);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao criptografar dados: " + e.getMessage(), e);
        }
    }

    /**
     * Descriptografa dados usando Envelope Encryption
     *
     * @param encryptedData Objeto com DEK criptografada e dados criptografados
     * @return Texto descriptografado
     */
    public String decryptWithEnvelope(EncryptedData encryptedData) {
        try {
            SecretKey dek = decryptDEKWithVault(encryptedData.getEncryptedDEK());

            return decryptWithDEK(encryptedData.getEncryptedValue(), dek);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao descriptografar dados: " + e.getMessage(), e);
        }
    }

    /**
     * Gera uma DEK (Data Encryption Key) aleatória
     */
    private SecretKey generateDEK() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
        keyGen.init(256, new SecureRandom());
        return keyGen.generateKey();
    }

    /**
     * Criptografa dados usando a DEK
     */
    private String encryptWithDEK(String plaintext, SecretKey dek) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, dek);
        byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    /**
     * Descriptografa dados usando a DEK
     */
    private String decryptWithDEK(String encryptedText, SecretKey dek) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, dek);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
        return new String(decryptedBytes);
    }

    /**
     * Criptografa a DEK usando a KEK do Vault (Transit Engine)
     */
    private String encryptDEKWithVault(SecretKey dek) {
        String dekBase64 = Base64.getEncoder().encodeToString(dek.getEncoded());

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Vault-Token", vaultToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("plaintext", dekBase64);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                vaultUri + TRANSIT_ENCRYPT_PATH,
                HttpMethod.POST,
                request,
                Map.class
        );

        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        return (String) data.get("ciphertext");
    }

    /**
     * Descriptografa a DEK usando a KEK do Vault (Transit Engine)
     */
    private SecretKey decryptDEKWithVault(String encryptedDEK) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Vault-Token", vaultToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("ciphertext", encryptedDEK);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                vaultUri + TRANSIT_DECRYPT_PATH,
                HttpMethod.POST,
                request,
                Map.class
        );

        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        String dekBase64 = (String) data.get("plaintext");

        byte[] dekBytes = Base64.getDecoder().decode(dekBase64);
        return new SecretKeySpec(dekBytes, ALGORITHM);
    }

    /**
     * Classe interna para armazenar dados criptografados
     */
    public static class EncryptedData {
        private String encryptedValue;
        private String encryptedDEK;

        public EncryptedData() {}

        public EncryptedData(String encryptedValue, String encryptedDEK) {
            this.encryptedValue = encryptedValue;
            this.encryptedDEK = encryptedDEK;
        }

        public String getEncryptedValue() {
            return encryptedValue;
        }

        public void setEncryptedValue(String encryptedValue) {
            this.encryptedValue = encryptedValue;
        }

        public String getEncryptedDEK() {
            return encryptedDEK;
        }

        public void setEncryptedDEK(String encryptedDEK) {
            this.encryptedDEK = encryptedDEK;
        }
    }
}