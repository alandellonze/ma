package it.ade.ma.api.controller;

import it.ade.ma.api.sevice.discography.DiscographyService;
import it.ade.ma.api.sevice.discography.model.DiscographyItem;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/discography")
public class DiscographyController {

    private final DiscographyService discographyService;

    @PatchMapping
    public void equal(@RequestBody DiscographyItem discographyItem) {
        discographyService.equal(discographyItem.getBandId(), discographyItem.getDiff());
    }

    @PostMapping
    public void plus(@RequestBody DiscographyItem discographyItem) {
        discographyService.plus(discographyItem.getBandId(), discographyItem.getDiff());
    }

    @PutMapping
    public void change(@RequestBody DiscographyItem discographyItem) {
        discographyService.change(discographyItem.getBandId(), discographyItem.getDiff());
    }

    @DeleteMapping("/{bandId}/{albumId}")
    public void minus(@PathVariable long bandId, @PathVariable long albumId) {
        discographyService.minus(bandId, albumId);
    }

}
