package de.jsyn.unifi.controller.tools;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Properties;

public class MailTool {

    Properties mailProps;
    Session mailSession;

    private MailTool(Properties mailProps, String username, String password) {
        this.mailProps = mailProps;
        mailSession = Session.getInstance(mailProps, createAuthenticator(username, password));
    }

    public void sendMessage(String from, String to, String subject, String msg) throws MessagingException {
        Message message = new MimeMessage(mailSession);
        message.setFrom(new InternetAddress(from));
        message.setRecipients(
                Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);

        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(msg, "text/plain");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);

        message.setContent(multipart);

        Transport.send(message);
    }

    private Authenticator createAuthenticator(String username, String password) {
        return new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        };
    }

    public static class MailToolBuilder {
        private String host;
        private String sslTrust;
        private String username;
        private String password;
        private boolean statTlsEnable = true;
        private boolean smtpAuth = true;
        private int port = 587;

        public MailToolBuilder setHost(String host) {
            this.host = host;
            this.sslTrust = host;
            return this;
        }

        public MailToolBuilder setUsername(String username) {
            this.username = username;
            return this;
        }

        public MailToolBuilder setPassword(String password) {
            this.password = password;
            return this;
        }

        public MailToolBuilder setPort(int port) {
            this.port = port;
            return this;
        }

        public MailToolBuilder setStartTls(boolean stattlsEnable) {
            this.statTlsEnable = stattlsEnable;
            return this;
        }

        public MailToolBuilder setSmtpAuth(boolean smtpAuth) {
            this.smtpAuth = smtpAuth;
            return this;
        }

        public MailTool build() {
            if (host == null || sslTrust == null || password == null || username == null) {
                throw new IllegalArgumentException("Host is not specified");
            }
            Properties prop = new Properties();
            prop.put("mail.smtp.auth", smtpAuth);
            prop.put("mail.smtp.starttls.enable", statTlsEnable);
            prop.put("mail.smtp.host", host);
            prop.put("mail.smtp.port", port);
            prop.put("mail.smtp.ssl.trust", sslTrust);
            return new MailTool(prop, username, password);
        }
    }


}