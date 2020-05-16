package it.ade.ma.api.controller;

import it.ade.ma.api.model.dto.DiscographyItem;
import it.ade.ma.api.model.dto.DiscographyResult;
import it.ade.ma.api.sevice.DiscographyService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/discography")
public class DiscographyController {

    private final static Logger logger = LoggerFactory.getLogger(DiscographyController.class);

    private DiscographyService discographyService;

    @GetMapping("/all/adjustPositions")
    public ResponseEntity adjustPositions() {
        logger.info("adjustPositions()");

        try {
            discographyService.adjustPositions();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/all")
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


    @GetMapping
    public ResponseEntity getOne(@RequestParam String bandName, @RequestParam(defaultValue = "false") boolean sendNotification) {
        logger.info("getOne({}, {})", bandName, sendNotification);

        try {
            DiscographyResult discographyResult = discographyService.execute(bandName, sendNotification);
            return ResponseEntity.ok(discographyResult);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping
    public ResponseEntity equal(@RequestBody DiscographyItem discographyItem) {
        logger.info("equal({})", discographyItem);

        try {
            discographyService.equal(discographyItem.getBand(), discographyItem.getAlbumDiff());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
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

    @PutMapping
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
