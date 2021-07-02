package it.ade.ma.api.sevice.path;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ma.path")
public class PathConfiguration {

    private String tmp;
    private String covers;
    private String mp3;
    private String scans;

}
