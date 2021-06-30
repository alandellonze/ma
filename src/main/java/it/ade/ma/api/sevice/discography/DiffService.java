package it.ade.ma.api.sevice.discography;

import it.ade.ma.api.sevice.covers.CoversService;
import it.ade.ma.api.sevice.db.model.Band;
import it.ade.ma.api.sevice.db.model.dto.AlbumDTO;
import it.ade.ma.api.sevice.db.repository.BandRepository;
import it.ade.ma.api.sevice.db.service.AlbumService;
import it.ade.ma.api.sevice.diff.model.DiffResult;
import it.ade.ma.api.sevice.discography.model.DiffResponse;
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

    public DiffResponse execute(String bandName) {
        log.info("execute({})", bandName);

        // get Band from db
        Optional<Band> bandOpt = bandRepository.findByName(bandName);
        if (bandOpt.isEmpty()) {
            log.info("band with name {} wasn't found", bandName);
            return null;
        }
        Band band = bandOpt.get();

        DiffResponse diffResponse = new DiffResponse();
        diffResponse.setBand(band);

        // get Albums from db
        List<AlbumDTO> albums = albumService.findAllByBandId(band.getId());
        log.info("{} albums found on db for band {}", albums.size(), band.getName());

        // FIXME search MP3 and Covers for each Albums
        mp3Service.findAndUpdate(albums);
        coversService.findAndUpdate(albums);

        // diffs from web
        diffResponse.setAlbumDiff(diffsWeb(band.getMaKey(), albums));

        // diffs covers
        diffResponse.setCoversDiff(diffsCovers(band.getName(), albums));

        // diffs mp3
        diffResponse.setMp3Diff(diffsMP3(band.getName(), albums));

        // diffs scans
        diffResponse.setScansDiff(diffsScans(band.getName(), albums));

        return diffResponse;
    }

    @SneakyThrows
    DiffResult<AlbumDTO> diffsWeb(Long maKey, List<AlbumDTO> albums) {
        log.info("diffsWeb({}, {})", maKey, albums.size());

        // get album from web
        List<AlbumDTO> albumsFromWeb = Objects.isNull(maKey) ? Collections.emptyList() : ripperService.execute(maKey);

        // diff album
        return albumDiffService.execute(albums, albumsFromWeb);
    }

    @SneakyThrows
    private DiffResult<String> diffsCovers(String bandName, List<AlbumDTO> albums) {
        log.info("diffsCovers({}, {})", bandName, albums.size());

        // convert album to cover's name
        List<String> albumsToString = albums.stream()
                .map(PathUtil::generateCoverName)
                .sorted()
                .collect(Collectors.toList());

        // get cover files from disk
        List<String> covers = coversService.getAllCovers(bandName);

        // diff covers
        return stringDiffService.execute(albumsToString, covers);
    }

    @SneakyThrows
    private DiffResult<String> diffsMP3(String bandName, List<AlbumDTO> albums) {
        log.info("diffsMP3({}, {})", bandName, albums.size());

        // convert album to mp3 folder's name
        List<String> albumsToString = albums.stream()
                .map(PathUtil::generateMP3Name)
                .sorted()
                .collect(Collectors.toList());

        // get mp3 folder's names from disk
        List<String> covers = mp3Service.getAllMP3s(bandName);

        // diff covers
        return stringDiffService.execute(albumsToString, covers);
    }

    @SneakyThrows
    private DiffResult<String> diffsScans(String bandName, List<AlbumDTO> albums) {
        log.info("diffsScans({}, {})", bandName, albums.size());

        // convert album to scan's name
        List<String> albumsToString = albums.stream()
                .map(PathUtil::generateScanName)
                .sorted()
                .collect(Collectors.toList());

        // get scan files from disk
        List<String> covers = coversService.getAllScans(bandName);

        // diff scans
        return stringDiffService.execute(albumsToString, covers);
    }

}
