package it.ade.ma.api.sevice.mail;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ma.mail")
public class MailConfiguration {

    private String from;
    private String fromName;
    private String to;

}
