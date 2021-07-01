package it.ade.ma.api.controller;

import it.ade.ma.api.sevice.diff.DiffService;
import it.ade.ma.api.sevice.diff.model.DiffResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/diffs")
public class DiffController {

    private final DiffService diffService;

    @GetMapping
    public ResponseEntity<DiffResponse> diffByBandId(@RequestParam long bandId) {
        return ResponseEntity.ok().body(diffService.diffByBandId(bandId));
    }

    @GetMapping("/all")
    public void diffAll() {
        diffService.diffAll();
    }


}
