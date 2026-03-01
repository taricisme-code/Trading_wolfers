package com.tradingdemo.notification;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import java.util.Properties;

public class SMTPNotifier implements Notifier {
    private final Session session;
    private final String from;

    public SMTPNotifier() throws Exception {
        EmailConfig cfg = new EmailConfig();
        String host = cfg.get("SMTP_HOST");
        String port = cfg.get("SMTP_PORT");
        String user = cfg.get("SMTP_USER");
        String pass = cfg.get("SMTP_PASS");
        String fromEnv = cfg.get("ALERT_FROM_EMAIL");

        if (host == null || host.isEmpty() || fromEnv == null || fromEnv.isEmpty()) {
            throw new IllegalStateException("SMTP not configured (SMTP_HOST and ALERT_FROM_EMAIL required)");
        }

        this.from = fromEnv;
        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        if (port != null && !port.isEmpty()) props.put("mail.smtp.port", port);
        boolean auth = user != null && !user.isEmpty();
        props.put("mail.smtp.auth", String.valueOf(auth));
        props.put("mail.smtp.starttls.enable", "true");

        if (auth) {
            final String u = user; final String p = pass;
            this.session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(u, p);
                }
            });
        } else {
            this.session = Session.getInstance(props);
        }
    }

    @Override
    public void sendEmail(String to, String subject, String body) throws Exception {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setText(body);
        Transport.send(message);
    }
}
