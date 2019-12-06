package it.ade.ma.api.controller;

import it.ade.ma.api.model.dto.DiscographyItem;
import it.ade.ma.api.model.dto.DiscographyResult;
import it.ade.ma.api.sevice.DiscographyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/discography")
public class DiscographyController {

    private final static Logger logger = LoggerFactory.getLogger(DiscographyController.class);

    @Autowired
    private DiscographyService discographyService;

    @GetMapping("/adjustAllPositions")
    public ResponseEntity adjustAllPositions() {
        logger.info("adjustAllPositions()");

        try {
            discographyService.adjustAllPositions();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping()
    public ResponseEntity getAll() {
        logger.info("getAll()");

        try {
            discographyService.executeAll();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }


    @GetMapping("/{bandName}")
    public ResponseEntity getOne(@PathVariable String bandName, @RequestParam(defaultValue = "false") boolean sendNotification) {
        logger.info("getOne({}, {})", bandName, sendNotification);

        try {
            DiscographyResult discographyResult = discographyService.execute(bandName, sendNotification);
            return ResponseEntity.ok(discographyResult);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping()
    public ResponseEntity plus(@RequestBody DiscographyItem discographyItem) {
        logger.info("plus({})", discographyItem);

        try {
            discographyService.plus(discographyItem.getBand(), discographyItem.getAlbumDiff());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping()
    public ResponseEntity change(@RequestBody DiscographyItem discographyItem) {
        logger.info("change({})", discographyItem);

        try {
            discographyService.change(discographyItem.getBand(), discographyItem.getAlbumDiff());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{albumId}")
    public ResponseEntity minus(@PathVariable Long albumId) {
        logger.info("minus({})", albumId);

        try {
            discographyService.minus(albumId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

}
