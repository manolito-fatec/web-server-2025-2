package com.pardal.app.service.vault;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Service responsible for implementing **Envelope Encryption** by utilizing a local
 * Data Encryption Key (DEK) and HashiCorp Vault's Transit Secret Engine for
 * managing the Key Encryption Key (KEK).
 * <p>
 * This approach encrypts data locally for performance while securely wrapping
 * the DEK using a master key (KEK) stored in Vault.
 * </p>
 */
@Service
@Slf4j
public class VaultEncryptionService {

    @Value("${spring.cloud.vault.uri}")
    private String vaultUri;

    @Value("${spring.cloud.vault.token}")
    private String vaultToken;

    private static final String TRANSIT_ENCRYPT_PATH = "/v1/transit/encrypt/user-encryption-key";
    private static final String TRANSIT_DECRYPT_PATH = "/v1/transit/decrypt/user-encryption-key";

    private static final String ALGORITHM_AES = "AES";
    private static final String ALGORITHM_CIPHER = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Encrypts plaintext data using **Envelope Encryption** (KEK + DEK).
     * <p>
     * The process involves:
     * <ol>
     * <li>Generating a random local Data Encryption Key (DEK).</li>
     * <li>Encrypting the {@code plaintext} using the generated DEK.</li>
     * <li>Encrypting the DEK itself using the Key Encryption Key (KEK) managed by Vault's Transit Engine.</li>
     * </ol>
     * </p>
     *
     * @param plaintext The text to be encrypted.
     * @return An {@link EncryptedData} object containing the ciphertext (encrypted with DEK)
     * and the wrapped DEK (encrypted with KEK/Vault).
     * @throws RuntimeException if any cryptographic or communication error occurs during the process.
     * @see EncryptedData
     */
    public EncryptedData encryptWithEnvelope(String plaintext) {
        try {
            SecretKey dek = generateDEK();

            String encryptedData = encryptWithDEK(plaintext, dek);

            String encryptedDEK = encryptDEKWithVault(dek);

            return new EncryptedData(encryptedData, encryptedDEK);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("Erro ao criptografar dados: " + e.getMessage(), e);
        }
    }

    /**
     * Decrypts data previously encrypted using the Envelope Encryption process.
     * <p>
     * The process involves:
     * <ol>
     * <li>Sending the encrypted DEK to Vault to be unwrapped (decrypted) using the KEK.</li>
     * <li>Using the resulting plaintext DEK to decrypt the cipher value.</li>
     * </ol>
     * </p>
     *
     * @param encryptedData The {@link EncryptedData} object containing the DEK encrypted by Vault
     * and the data encrypted by the DEK.
     * @return The original plaintext string.
     * @throws RuntimeException if any cryptographic or communication error occurs during the process.
     * @see EncryptedData
     */
    public String decryptWithEnvelope(EncryptedData encryptedData) {
        try {
            SecretKey dek = decryptDEKWithVault(encryptedData.getEncryptedDEK());

            return decryptWithDEK(encryptedData.getEncryptedValue(), dek);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("Erro ao descriptografar dados: " + e.getMessage(), e);
        }
    }

    /**
     * Generates a random, high-strength Data Encryption Key (DEK) using AES-256.
     *
     * @return A newly generated {@link SecretKey}.
     * @throws Exception If the AES algorithm is unavailable or key generation fails.
     */
    public SecretKey generateDEK() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM_AES);
        keyGen.init(256, new SecureRandom());
        return keyGen.generateKey();
    }

    /**
     * Encrypts the provided plaintext using the given DEK (Data Encryption Key).
     * <p>
     * This implementation uses AES/GCM/NoPadding with a random 12-byte IV (Nonce).
     * The final output is Base64-encoded [IV + Ciphertext].
     * </p>
     *
     * @param plaintext The data string to encrypt.
     * @param dek The symmetric secret key to use for encryption.
     * @return The Base64-encoded string: [IV + Ciphertext].
     * @throws Exception If the Cipher instance cannot be initialized or the encryption fails.
     */
    public String encryptWithDEK(String plaintext, SecretKey dek) throws Exception {
        byte[] iv = new byte[IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);

        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);

        Cipher cipher = Cipher.getInstance(ALGORITHM_CIPHER);
        cipher.init(Cipher.ENCRYPT_MODE, dek, gcmSpec);

        byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes());

        byte[] combined = new byte[IV_LENGTH + encryptedBytes.length];
        System.arraycopy(iv, 0, combined, 0, IV_LENGTH);
        System.arraycopy(encryptedBytes, 0, combined, IV_LENGTH, encryptedBytes.length);

        return Base64.getEncoder().encodeToString(combined);
    }
    /**
     * Decrypts a Base64-encoded ciphertext [IV + Ciphertext] using the provided DEK.
     *
     * @param encryptedText The Base64-encoded [IV + Ciphertext] string.
     * @param dek The symmetric secret key to use for decryption.
     * @return The decrypted plaintext string.
     * @throws Exception If the Cipher instance cannot be initialized or the decryption fails.
     */
    public String decryptWithDEK(String encryptedText, SecretKey dek) throws Exception {

        byte[] combined = Base64.getDecoder().decode(encryptedText);

        byte[] iv = new byte[IV_LENGTH];
        System.arraycopy(combined, 0, iv, 0, IV_LENGTH);

        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);

        int ciphertextLength = combined.length - IV_LENGTH;
        byte[] ciphertext = new byte[ciphertextLength];
        System.arraycopy(combined, IV_LENGTH, ciphertext, 0, ciphertextLength);

        Cipher cipher = Cipher.getInstance(ALGORITHM_CIPHER);
        cipher.init(Cipher.DECRYPT_MODE, dek, gcmSpec);

        byte[] decryptedBytes = cipher.doFinal(ciphertext);

        return new String(decryptedBytes);
    }

    /**
     * Encrypts the raw DEK bytes using the Key Encryption Key (KEK) managed by Vault's Transit Engine
     * via a REST API call.
     * <p>
     * The DEK is first Base64-encoded before being sent as the {@code plaintext} parameter to Vault.
     * </p>
     *
     * @param dek The plaintext DEK to be protected.
     * @return The ciphertext string returned by Vault (containing the encrypted DEK).
     * @throws org.springframework.web.client.RestClientException if the Vault API call fails.
     */
    public String encryptDEKWithVault(SecretKey dek) {
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
     * Decrypts the Vault-wrapped DEK using the Key Encryption Key (KEK) managed by Vault's Transit Engine
     * via a REST API call.
     *
     * @param encryptedDEK The ciphertext containing the DEK, as returned by Vault's encrypt endpoint.
     * @return The unwrapped (plaintext) DEK as a {@link SecretKey} object.
     * @throws org.springframework.web.client.RestClientException if the Vault API call fails.
     */
    public SecretKey decryptDEKWithVault(String encryptedDEK) {
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

        return new SecretKeySpec(dekBytes, ALGORITHM_AES);
    }

    /**
     * A simple data class used to encapsulate the result of the Envelope Encryption process.
     * <p>
     * It stores the data encrypted by the DEK and the DEK itself, encrypted by Vault's KEK.
     * This pair is necessary for later decryption.
     * </p>
     */
    public static class EncryptedData {
        private String encryptedValue;
        private String encryptedDEK;

        /**
         * Default constructor required for serialization (e.g., JSON).
         */
        public EncryptedData() {}

        /**
         * Constructor for creating a new EncryptedData instance.
         *
         * @param encryptedValue The data encrypted with the DEK.
         * @param encryptedDEK The DEK encrypted with Vault's KEK.
         */
        public EncryptedData(String encryptedValue, String encryptedDEK) {
            this.encryptedValue = encryptedValue;
            this.encryptedDEK = encryptedDEK;
        }

        /**
         * Gets the data encrypted with the DEK.
         * @return The encrypted data string.
         */
        public String getEncryptedValue() {
            return encryptedValue;
        }

        /**
         * Sets the data encrypted with the DEK.
         * @param encryptedValue The encrypted data string.
         */
        public void setEncryptedValue(String encryptedValue) {
            this.encryptedValue = encryptedValue;
        }

        /**
         * Gets the DEK encrypted with Vault's KEK.
         * @return The encrypted DEK string (ciphertext returned by Vault).
         */
        public String getEncryptedDEK() {
            return encryptedDEK;
        }

        /**
         * Sets the DEK encrypted with Vault's KEK.
         * @param encryptedDEK The encrypted DEK string (ciphertext returned by Vault).
         */
        public void setEncryptedDEK(String encryptedDEK) {
            this.encryptedDEK = encryptedDEK;
        }
    }
}