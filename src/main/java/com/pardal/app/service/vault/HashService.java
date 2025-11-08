package com.pardal.app.service.vault;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class HashService {

    /**
     * Gera hash SHA-256 de um texto (para busca)
     * Usado para criar índice pesquisável de emails criptografados
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


    public boolean verifyEmailHash(String email, String hash) {
        String emailHash = hashEmail(email);
        return emailHash != null && emailHash.equals(hash);
    }
}