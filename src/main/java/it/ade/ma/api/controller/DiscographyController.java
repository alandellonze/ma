package it.ade.ma.api.controller;

import it.ade.ma.api.model.dto.DiscographyResult;
import it.ade.ma.api.sevice.DiscographyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/discography")
public class DiscographyController {

    @Autowired
    private DiscographyService discographyService;

    @GetMapping()
    public ResponseEntity getDiscographys() {
        try {
            discographyService.executeAll();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{bandName}")
    public ResponseEntity getDiscography(@PathVariable String bandName, @RequestParam(defaultValue = "false") boolean sendNotification) {
        try {
            DiscographyResult discographyResult = discographyService.execute(bandName, sendNotification);
            return ResponseEntity.ok(discographyResult);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

}
