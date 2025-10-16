package com.pardal.app.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String sender;

    public String sendValidationEmail(String recipient, String token) {
        String baseUrl = "https://localhost:8080";
        String verificationUrl = baseUrl + "/api/auth/verify?token=" + token;

        String subject = "Confirmação do seu cadastro na plataforma Pardal!";

        String htmlContent = """
    <html>
        <body>
            <img src="cid:logo" alt="Logo" style="width: 150px; height: auto;">
            <h1>Bem-vindo à plataforma Pardal!</h1>
            <p>Olá, clique <a href="%s">aqui</a> para confirmar seu e-mail.</p>
        </body>
    </html>
    """.formatted(verificationUrl);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setTo(recipient);
            helper.setFrom(sender);

            ClassPathResource image = new ClassPathResource("images/logo.png");
            helper.addInline("logo", image);

            mailSender.send(message);
            return "Email sent successfully!";
        } catch (MessagingException e) {
            e.printStackTrace();
            return "Error sending email.";
        }
    }
}