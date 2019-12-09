package it.ade.ma.api.controller;

import it.ade.ma.api.sevice.MP3Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mp3")
public class MP3Controller {

    private final static Logger logger = LoggerFactory.getLogger(MP3Controller.class);

    @Autowired
    private MP3Service mp3Service;


    @GetMapping("/test")
    public ResponseEntity test() {
        logger.info("test()");

        try {
            mp3Service.adjustAlbumFolder();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

}
