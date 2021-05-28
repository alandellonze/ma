package it.ade.ma.api.sevice.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.internet.MimeMessage;

@Slf4j
@Component
@RequiredArgsConstructor
public class MailService {

    private final MailConfiguration mailConfiguration;
    private final JavaMailSender javaMailSender;

    public void sendEmail(String subject, String text) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(mailConfiguration.getFrom(), mailConfiguration.getFromName());
            helper.setReplyTo(mailConfiguration.getFrom());
            helper.setTo(mailConfiguration.getTo());
            helper.setSubject(subject);
            helper.setText(text, true);

            javaMailSender.send(message);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

}
