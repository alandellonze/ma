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
        // FIXME to be implemented: plus
        System.out.println("plus");
        System.out.println(discographyItem);
        return ResponseEntity.ok().build();
    }

    @PutMapping()
    public ResponseEntity change(@RequestBody DiscographyItem discographyItem) {
        // FIXME to be implemented: change
        System.out.println("change");
        System.out.println(discographyItem);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{albumId}")
    public ResponseEntity minus(@PathVariable Long albumId) {
        // FIXME to be implemented: minus
        System.out.println("minus");
        System.out.println(albumId);
        return ResponseEntity.ok().build();
    }

}
