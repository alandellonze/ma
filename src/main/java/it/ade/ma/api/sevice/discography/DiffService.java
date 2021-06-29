package it.ade.ma.api.sevice.discography;

import it.ade.ma.api.sevice.covers.CoversService;
import it.ade.ma.api.sevice.db.model.Band;
import it.ade.ma.api.sevice.db.model.dto.AlbumDTO;
import it.ade.ma.api.sevice.db.repository.BandRepository;
import it.ade.ma.api.sevice.db.service.AlbumService;
import it.ade.ma.api.sevice.diff.model.DiffResult;
import it.ade.ma.api.sevice.mp3.MP3Service;
import it.ade.ma.api.sevice.path.PathUtil;
import it.ade.ma.api.sevice.ripper.RipperService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiffService {

    private final BandRepository bandRepository;
    private final AlbumService albumService;

    private final RipperService ripperService;
    private final CoversService coversService;
    private final MP3Service mp3Service;

    private final AlbumDiffService albumDiffService;
    private final StringDiffService stringDiffService;

    public void execute(String bandName) {
        log.info("execute({})", bandName);

        // get Band from db
        Optional<Band> bandOpt = bandRepository.findByName(bandName);
        if (bandOpt.isEmpty()) {
            log.info("band with name {} wasn't found", bandName);
        } else {
            Band band = bandOpt.get();

            // get Albums from db
            List<AlbumDTO> albums = albumService.findAllByBandName(band.getName());
            log.info("{} albums found on db for band {}", albums.size(), bandName);

            // TODO diffs from web
            diffsWeb(band, albums);

            // TODO diffs covers
            diffsCovers(band, albums);

            // TODO diffs mp3
            diffsMP3(band, albums);

            // TODO diffs scans
            diffsScans(band, albums);
        }
    }

    @SneakyThrows
    DiffResult<AlbumDTO> diffsWeb(Band band, List<AlbumDTO> albums) {
        log.info("diffsWeb({}, {})", band, albums.size());

        // get album from web
        List<AlbumDTO> albumsFromWeb = Objects.isNull(band.getMaKey()) ? Collections.emptyList() : ripperService.execute(band.getMaKey());

        // diff album
        return albumDiffService.execute(albums, albumsFromWeb);
    }

    @SneakyThrows
    private void diffsCovers(Band band, List<AlbumDTO> albums) {
        log.info("diffsCovers({}, {})", band, albums.size());

        // convert album to cover's name
        List<String> albumsToString = albums.stream()
                .map(PathUtil::generateCoverName)
                .sorted()
                .collect(Collectors.toList());

        // get cover files from disk
        List<String> covers = coversService.getAllCovers(band.getName());

        // diff covers
        stringDiffService.execute(albumsToString, covers);
    }

    @SneakyThrows
    private void diffsMP3(Band band, List<AlbumDTO> albums) {
        log.info("diffsMP3({}, {})", band, albums.size());

        // convert album to mp3 folder's name
        List<String> albumsToString = albums.stream()
                .map(PathUtil::generateMP3Name)
                .sorted()
                .collect(Collectors.toList());

        // get mp3 folder's names from disk
        List<String> covers = mp3Service.getAllMP3s(band.getName());

        // diff covers
        stringDiffService.execute(albumsToString, covers);
    }

    @SneakyThrows
    private void diffsScans(Band band, List<AlbumDTO> albums) {
        log.info("diffsScans({}, {})", band, albums.size());

        // convert album to scan's name
        List<String> albumsToString = albums.stream()
                .map(PathUtil::generateScanName)
                .sorted()
                .collect(Collectors.toList());

        // get scan files from disk
        List<String> covers = coversService.getAllScans(band.getName());

        // diff scans
        stringDiffService.execute(albumsToString, covers);
    }

}
