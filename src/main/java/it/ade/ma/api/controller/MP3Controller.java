package it.ade.ma.api.controller;

import it.ade.ma.api.sevice.MP3Service;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/mp3")
public class MP3Controller {

    private final static Logger logger = LoggerFactory.getLogger(MP3Controller.class);

    private MP3Service mp3Service;

    @GetMapping("/check/{albumId}")
    public ResponseEntity check(@PathVariable Long albumId) {
        logger.info("check({})", albumId);

        try {
            mp3Service.check(albumId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

}
