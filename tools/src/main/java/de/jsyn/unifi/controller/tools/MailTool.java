package de.jsyn.unifi.controller.tools;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class MailTool implements MessagingTool {

    public static final String USERNAME_PROPERTY = "mailtool.username";
    public static final String PASSWORD_PROPERTY = "mailtool.password";
    public static final String FROM_PROPERTY = "mailtool.from";
    public static final String TO_PROPERTY = "mailtool.to";
    private Properties mailProperties;
    private Session mailSession;

    public MailTool(Properties properties) {
        this.mailProperties = properties;
        this.mailSession = Session.getInstance(mailProperties, createAuthenticator(properties.getProperty(USERNAME_PROPERTY), properties.getProperty(PASSWORD_PROPERTY)));
    }

    private static Authenticator createAuthenticator(String username, String password) {
        return new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        };
    }

    @Override
    public void sendMessage(String subject, String msg, File qrcode) {
        try {
            Message message = new MimeMessage(mailSession);
            message.setFrom(new InternetAddress(mailProperties.getProperty(FROM_PROPERTY)));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mailProperties.getProperty(TO_PROPERTY)));
            message.setSubject(subject);

            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setContent(msg, "text/plain");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(mimeBodyPart);

            if (qrcode != null) {
                MimeBodyPart code = new MimeBodyPart();
                code.setContent(msg, "image/png");
                code.attachFile(qrcode);
                multipart.addBodyPart(code);
            }

            message.setContent(multipart);

            Transport.send(message);
        } catch (MessagingException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class MailPropertiesBuilder {
        private String host;
        private String sslTrust;
        private String username;
        private String password;
        private boolean statTlsEnable = true;
        private boolean smtpAuth = true;
        private int port = 587;
        private String from;
        private String to;

        public MailPropertiesBuilder setHost(String host) {
            if (host == null || host.isEmpty()) throw new IllegalArgumentException();
            this.host = host;
            this.sslTrust = host;
            return this;
        }

        public MailPropertiesBuilder setUsername(String username) {
            if (username == null) throw new IllegalArgumentException();
            this.username = username;
            return this;
        }

        public MailPropertiesBuilder setPassword(String password) {
            this.password = password;
            return this;
        }

        public MailPropertiesBuilder setPort(int port) {
            this.port = port;
            return this;
        }

        public MailPropertiesBuilder setStartTls(boolean stattlsEnable) {
            this.statTlsEnable = stattlsEnable;
            return this;
        }

        public MailPropertiesBuilder setSmtpAuth(boolean smtpAuth) {
            this.smtpAuth = smtpAuth;
            return this;
        }

        public MailPropertiesBuilder setFrom(String from) {
            if (from == null || from.isEmpty()) throw new IllegalArgumentException();
            this.from = from;
            return this;
        }

        public MailPropertiesBuilder setTo(String to) {
            if (to == null || to.isEmpty()) throw new IllegalArgumentException();
            this.to = to;
            return this;
        }

        public Properties build() {
            Properties properties = new Properties();
            properties.put("mail.smtp.auth", smtpAuth);
            properties.put("mail.smtp.starttls.enable", statTlsEnable);
            properties.put("mail.smtp.host", host);
            properties.put("mail.smtp.port", port);
            properties.put("mail.smtp.ssl.trust", sslTrust);
            properties.put(FROM_PROPERTY, from);
            properties.put(TO_PROPERTY, to);
            properties.put(USERNAME_PROPERTY, username);
            properties.put(PASSWORD_PROPERTY, password);
            return properties;
        }
    }
}