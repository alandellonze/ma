package it.ade.ma.api.controller;

import it.ade.ma.api.sevice.mp3.MP3Service;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/mp3")
public class MP3Controller {

    private MP3Service mp3Service;

    @GetMapping("/check/{albumId}")
    public ResponseEntity check(@PathVariable Long albumId) {
        log.info("check({})", albumId);

        try {
            mp3Service.check(albumId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

}
