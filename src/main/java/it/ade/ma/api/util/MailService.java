package it.ade.ma.api.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.internet.MimeMessage;

@Component
public class MailService {

    private final static Logger logger = LoggerFactory.getLogger(MailService.class);

    private String from;
    private String fromName;
    private String to;
    private JavaMailSender javaMailSender;

    public MailService(
            @Value("${mail.from}") String from,
            @Value("${mail.fromName}") String fromName,
            @Value("${mail.to}") String to,
            JavaMailSender javaMailSender) {
        this.from = from;
        this.fromName = fromName;
        this.to = to;
        this.javaMailSender = javaMailSender;
    }

    public void sendEmail(String subject, String text) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(from, fromName);
            helper.setReplyTo(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, true);

            javaMailSender.send(message);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

}
