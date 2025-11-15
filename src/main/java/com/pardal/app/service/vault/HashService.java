package com.pardal.app.service.vault;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class HashService {

    /**
     * Generates a **SHA-256 hash** of an email address.
     * <p>
     * The email is first normalized by converting it to lowercase and trimming
     * whitespace before hashing. This method is typically used to create a
     * searchable index (a hash prefix) for finding encrypted user records
     * without exposing the original email directly.
     * </p>
     * @param email the email address string to be hashed.
     * @return the SHA-256 hash string in hexadecimal format, or {@code null} if
     * the input email is {@code null} or empty.
     * @throws RuntimeException if the SHA-256 algorithm cannot be found in the
     * security provider.
     */
    public String hashEmail(String email) {
        if (email == null || email.isEmpty()) {
            return null;
        }

        try {
            String normalizedEmail = email.toLowerCase().trim();

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(normalizedEmail.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao gerar hash do email", e);
        }
    }

    /**
     * Verifies if a given plain email address matches a stored SHA-256 hash.
     * <p>
     * This is accomplished by hashing the provided {@code email} using the
     * {@link #hashEmail(String)} method and comparing the result with the
     * provided {@code hash}.
     * </p>
     * @param email the plain email address to check against the hash.
     * @param hash the pre-calculated SHA-256 hash string to compare with.
     * @return {@code true} if the hashed email matches the provided hash and is not
     * {@code null}; {@code false} otherwise.
     * @see #hashEmail(String)
     */
    public boolean verifyEmailHash(String email, String hash) {
        String emailHash = hashEmail(email);
        return emailHash != null && emailHash.equals(hash);
    }
}