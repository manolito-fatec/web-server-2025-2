package com.pardal.app.mail;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.spring6.SpringTemplateEngine;

import org.thymeleaf.context.Context;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailService {

    private final String SUBJECT_PRE_REGISTRATION = "Pré-cadastro na plataforma Pardal!";
    private final String PRE_REGISTRATION_TEMPLATE = "pre-registration";
    private final String SUBJECT_APPROVAL_REGISTRATION = "Seu Cadastro Pardal foi Aprovado!";
    private final String APPROVAL_TEMPLATE = "approval";
    private final String UTF_8 = "UTF-8";
    private final String PARDAL_IMAGE="images/logo.png";

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final String sender;
    private final String baseUrl;

    public EmailService(
            JavaMailSender mailSender,
            SpringTemplateEngine templateEngine,
            @Value("${spring.mail.username}") String sender,
            @Value("${app.base-url}") String baseUrl) {

        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.sender = sender;
        this.baseUrl = baseUrl;
    }

    /**
    * Constructs and sends an email using Thymeleaf template processing.
    * This method handles setting up the MIME message, embedding the logo image,
    * and sending the email via JavaMailSender.
    *
    * @param recipient the address to send the email to
    * @param subject the subject line of the email
    * @param templateName the name of the Thymeleaf template file (e.g., "email/template-name")
    * @param variables a map of data variables to be processed by the template
    * @author paulo arantes
    * @throws MessagingException if the message could not be created or sent
    */
    private void sendEmail(String recipient, String subject, String templateName, Map<String, Object> variables) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF_8); 

        Context context = new Context();
        context.setVariables(variables);

        String htmlContent = templateEngine.process(templateName, context); 

        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        helper.setTo(recipient);
        helper.setFrom(sender);

        ClassPathResource image = new ClassPathResource(PARDAL_IMAGE);
        helper.addInline("logo", image);

        mailSender.send(message);
    }

    /**
     * Sends the user pre-registration email asynchronously.
     *
     * @param recipient the email address of the recipient
     * @author paulo arantes
     * @return a CompletableFuture representing the completion of the email sending process
     */
    @Async
    public void sendPreRegistrationEmail(String recipient) {
        try {
            sendEmail(recipient, SUBJECT_PRE_REGISTRATION, PRE_REGISTRATION_TEMPLATE, Map.of());
            log.info("Email de pré-cadastro enviado com sucesso!");
        } catch (MessagingException e) {
            log.info("Erro ao enviar email de pré-cadastro.");
            e.getStackTrace();
        }
    }

    /**
     * Sends the user approval email asynchronously.
     *
     * @param recipient the email address of the recipient
     * @author paulo arantes
     * @return a CompletableFuture representing the completion of the email sending process
     */
    @Async
    public void sendApprovalEmail(String recipient) {
        try {
            String loginUrl = baseUrl;
            Map<String, Object> variables = Map.of(
                    "loginUrl", loginUrl
                    );
            sendEmail(recipient, SUBJECT_APPROVAL_REGISTRATION, APPROVAL_TEMPLATE, variables);
            log.info("Email de aprovação enviado com sucesso!");
        } catch (MessagingException e) {
            log.info("Erro ao enviar email de aprovação.");
            e.getStackTrace();
        }
    }
}