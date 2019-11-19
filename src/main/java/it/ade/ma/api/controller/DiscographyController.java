package it.ade.ma.api.controller;

import it.ade.ma.api.model.dto.DiscographyResult;
import it.ade.ma.api.sevice.DiscographyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/discography")
public class DiscographyController {

    @Autowired
    private DiscographyService discographyService;

    @GetMapping("/{bandName}")
    public ResponseEntity test(@PathVariable String bandName) {
        try {
            DiscographyResult discographyResult = discographyService.execute(bandName);
            return ResponseEntity.ok(discographyResult);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

}
