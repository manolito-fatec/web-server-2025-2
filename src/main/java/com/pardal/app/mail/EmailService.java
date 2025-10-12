package com.pardal.app.mail;

import com.mailersend.sdk.MailerSend;
import com.mailersend.sdk.MailerSendResponse;
import com.mailersend.sdk.emails.Email;
import com.mailersend.sdk.exceptions.MailerSendException;

public class EmailService {

    public void sendValidationEmail(String recipient, String token){
        String baseUrl = "https://sua-api.com";
        String verificationUrl = baseUrl + "/api/auth/verify?token=" + token;

        String subject = "Confirmação de Cadastro";
        String content = "Olá, clique no link abaixo para confirmar seu e-mail:\n" + verificationUrl;

        try{
            MailerSend ms = new MailerSend();
            ms.setToken("");

            Email email = new Email();
            email.subject = subject;
            email.text = content;
            email.addRecipient(recipient,recipient);
            email.setFrom("Pardal team", "no-reply@test-y7zpl98q6r045vx6.mlsender.net");

            MailerSendResponse response = ms.emails().send(email);



        } catch (MailerSendException e) {
            throw new RuntimeException(e);
        }
    }
}
