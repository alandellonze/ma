package it.ade.ma.api.controller;

import it.ade.ma.api.sevice.mp3.MP3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mp3")
@RequiredArgsConstructor
public class MP3Controller {

    private final MP3Service mp3Service;

    @GetMapping("/check/{albumId}")
    public void check(@PathVariable Long albumId) {
        mp3Service.check(albumId);
    }

}
