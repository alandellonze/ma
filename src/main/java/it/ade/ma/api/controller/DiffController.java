package it.ade.ma.api.controller;

import it.ade.ma.api.sevice.discography.DiffService;
import it.ade.ma.api.sevice.discography.model.DiffResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/diff")
@RequiredArgsConstructor
public class DiffController {

    private final DiffService diffService;

    @GetMapping
    public ResponseEntity<DiffResponse> diff(@RequestParam String bandName) {
        log.info("diff({})", bandName);

        return ResponseEntity.ok().body(diffService.execute(bandName));
    }

}
