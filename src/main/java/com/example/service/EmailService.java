package com.example.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.util.Properties;
@Service
public class EmailService {

    private final JavaMailSender emailSender;

    @Value("${spring.mail.username}")
    private String username;

    @Value("${DB_HOST}")
    private String dbHost;

    public EmailService(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    // Method to send a simple email
    public void sendEmail(String to, String subject, String message) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setFrom(username); // Sender email
        email.setTo(to); // Recipient email
        email.setSubject(subject); // Subject of the email
        email.setText(message); // Message body

        emailSender.send(email);
    }

    // Method to send invitation email with beautiful template
    public void sendEmailInvitation(String to, String channelName, String inviterName) {
        String subject = "🎉 Invitation à rejoindre un canal de discussion";
        
        String message = String.format("""
            Bonjour !

            🎊 Excellente nouvelle ! Vous avez été invité(e) à rejoindre un canal de discussion.

            📋 Détails de l'invitation :
            • Canal : %s
            • Invité par : %s
            • Plateforme : PowerChat

            🔗 Pour accepter cette invitation, cliquez sur le lien suivant :
            http://%s:3000

            📱 Une fois connecté(e), vous pourrez :
            • Rejoindre le canal
            • Participer aux discussions
            • Échanger avec les autres membres

            🚀 Nous vous souhaitons une excellente expérience de collaboration !

            Cordialement,
            L'équipe de l'Application de Chat

            ---
            💡 Besoin d'aide ? Contactez notre support technique.
            """, channelName, inviterName, dbHost);

        sendEmail(to, subject, message);
    }
}