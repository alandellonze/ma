package it.ade.ma.api.sevice.db.service;

import it.ade.ma.api.sevice.db.model.Band;
import it.ade.ma.api.sevice.db.model.dto.BandDTO;
import it.ade.ma.api.sevice.db.model.mapper.BandMapper;
import it.ade.ma.api.sevice.db.repository.BandRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BandService {

    private final BandMapper bandMapper;
    private final BandRepository bandRepository;

    public List<BandDTO> findAllForWeb() {
        return convert(bandRepository.findAllByMaKeyNotNullOrderByName());
    }

    public List<BandDTO> findAllByName(String name) {
        return convert(bandRepository.findAllByName(name));
    }

    public Optional<BandDTO> findById(long id) {
        return bandRepository.findById(id)
                .map(bandMapper::toBandDTO);
    }

    // UTIL

    private List<BandDTO> convert(List<Band> bands) {
        return bands
                .stream()
                .map(bandMapper::toBandDTO)
                .collect(Collectors.toList());
    }

}
