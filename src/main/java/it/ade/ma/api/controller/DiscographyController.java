package it.ade.ma.api.controller;

import it.ade.ma.api.sevice.discography.DiscographyService;
import it.ade.ma.api.sevice.discography.model.DiscographyItem;
import it.ade.ma.api.sevice.discography.model.DiscographyResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/discography")
public class DiscographyController {

    private final DiscographyService discographyService;

    @GetMapping("/all/adjustPositions")
    public ResponseEntity<HttpStatus> adjustPositions() {
        log.info("adjustPositions()");

        try {
            discographyService.adjustPositions();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<HttpStatus> getAll() {
        log.info("getAll()");

        try {
            discographyService.executeAll();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @Deprecated
    @GetMapping
    public ResponseEntity<DiscographyResult> getOne(@RequestParam String bandName, @RequestParam(defaultValue = "false") boolean sendNotification) {
        log.info("getOne({}, {})", bandName, sendNotification);

        DiscographyResult discographyResult = discographyService.execute(bandName, sendNotification);
        return ResponseEntity.ok().body(discographyResult);
    }

    @PatchMapping
    public ResponseEntity<HttpStatus> equal(@RequestBody DiscographyItem discographyItem) {
        log.info("equal({})", discographyItem);

        try {
            discographyService.equal(discographyItem.getBand(), discographyItem.getDiff());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    public ResponseEntity<HttpStatus> plus(@RequestBody DiscographyItem discographyItem) {
        log.info("plus({})", discographyItem);

        try {
            discographyService.plus(discographyItem.getBand(), discographyItem.getDiff());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping
    public ResponseEntity<HttpStatus> change(@RequestBody DiscographyItem discographyItem) {
        log.info("change({})", discographyItem);

        try {
            discographyService.change(discographyItem.getBand(), discographyItem.getDiff());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{albumId}")
    public ResponseEntity<HttpStatus> minus(@PathVariable Long albumId) {
        log.info("minus({})", albumId);

        try {
            discographyService.minus(albumId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

}
