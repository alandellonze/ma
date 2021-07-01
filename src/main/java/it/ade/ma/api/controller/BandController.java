package it.ade.ma.api.controller;

import it.ade.ma.api.sevice.db.model.dto.BandDTO;
import it.ade.ma.api.sevice.db.service.BandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bands")
public class BandController {

    private final BandService bandService;

    @GetMapping
    public ResponseEntity<List<BandDTO>> findAllByName(@RequestParam String name) {
        return ResponseEntity.ok().body(bandService.findAllByName(name));
    }

}
