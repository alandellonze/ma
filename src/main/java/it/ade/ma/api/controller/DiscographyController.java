package it.ade.ma.api.controller;

import it.ade.ma.api.sevice.discography.DiscographyService;
import it.ade.ma.api.sevice.discography.model.DiscographyItem;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/discography")
public class DiscographyController {

    private final DiscographyService discographyService;

    @GetMapping("/all/adjustPositions")
    public ResponseEntity<HttpStatus> adjustPositions() {
        try {
            discographyService.adjustPositions();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<HttpStatus> getAll() {
        try {
            discographyService.executeAll();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping
    public ResponseEntity<HttpStatus> equal(@RequestBody DiscographyItem discographyItem) {
        try {
            discographyService.equal(discographyItem.getBandId(), discographyItem.getDiff());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    public ResponseEntity<HttpStatus> plus(@RequestBody DiscographyItem discographyItem) {
        try {
            discographyService.plus(discographyItem.getBandId(), discographyItem.getDiff());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping
    public ResponseEntity<HttpStatus> change(@RequestBody DiscographyItem discographyItem) {
        try {
            discographyService.change(discographyItem.getBandId(), discographyItem.getDiff());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{bandId}/{albumId}")
    public ResponseEntity<HttpStatus> minus(@PathVariable long bandId, @PathVariable long albumId) {
        try {
            discographyService.minus(bandId, albumId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

}
