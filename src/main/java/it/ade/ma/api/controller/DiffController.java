package it.ade.ma.api.controller;

import it.ade.ma.api.sevice.discography.DiffService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/diff")
public class DiffController {

    private final DiffService diffService;

    @GetMapping
    public ResponseEntity<Void> diff(@RequestParam String bandName) {
        log.info("diff({})", bandName);

        diffService.execute(bandName);
        return ResponseEntity.ok().build();
    }

}
