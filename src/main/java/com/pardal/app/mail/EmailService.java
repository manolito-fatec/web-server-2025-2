package com.pardal.app.mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;


public class EmailService {


    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String sender;

    public String sendValidationEmail(String recipient, String token) {
        String baseUrl = "https://localhost:8080";
        String verificationUrl = baseUrl + "/api/auth/verify?token=" + token;

        String subject = "Confirmação de Cadastro";
        String content = "Olá, clique no link abaixo para confirmar seu e-mail:\n" + verificationUrl;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setText(content);
        message.setTo("caueherobrine@gmail.com");
        message.setFrom(sender);

        try {
            mailSender.send(message);
            return "Email sent successfully!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error sending email.";
        }
    }
}
