package it.ade.ma.api.controller;

import it.ade.ma.api.model.dto.DiscographyItem;
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

    @GetMapping("/adjustAllPositions")
    public ResponseEntity adjustAllPositions() {
        try {
            discographyService.adjustAllPositions();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping()
    public ResponseEntity getAll() {
        try {
            discographyService.executeAll();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }


    @GetMapping("/{bandName}")
    public ResponseEntity getOne(@PathVariable String bandName, @RequestParam(defaultValue = "false") boolean sendNotification) {
        try {
            DiscographyResult discographyResult = discographyService.execute(bandName, sendNotification);
            return ResponseEntity.ok(discographyResult);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping()
    public ResponseEntity plus(@RequestBody DiscographyItem discographyItem) {
        boolean result = discographyService.plus(discographyItem.getBand(), discographyItem.getAlbumDiff());
        if (result) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping()
    public ResponseEntity change(@RequestBody DiscographyItem discographyItem) {
        boolean result = discographyService.change(discographyItem.getBand(), discographyItem.getAlbumDiff());
        if (result) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{albumId}")
    public ResponseEntity minus(@PathVariable Long albumId) {
        boolean result = discographyService.minus(albumId);
        if (result) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

}
