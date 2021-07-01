package it.ade.ma.api.sevice.diff;

import it.ade.ma.api.sevice.covers.CoversService;
import it.ade.ma.api.sevice.db.model.dto.AlbumDTO;
import it.ade.ma.api.sevice.db.model.dto.BandDTO;
import it.ade.ma.api.sevice.db.service.AlbumService;
import it.ade.ma.api.sevice.db.service.BandService;
import it.ade.ma.api.sevice.diff.engine.model.DiffResult;
import it.ade.ma.api.sevice.diff.model.DiffResponse;
import it.ade.ma.api.sevice.diff.model.DiscographyResult;
import it.ade.ma.api.sevice.mail.NotificationService;
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

    private final BandService bandService;
    private final AlbumService albumService;

    private final RipperService ripperService;
    private final CoversService coversService;
    private final MP3Service mp3Service;

    private final AlbumDTODiffService albumDTODiffService;
    private final StringDiffService stringDiffService;

    private final NotificationService notificationService;

    public void diffAll() {
        log.info("executeAll()");

        bandService.findAllForWeb()
                .forEach(this::diffAll);
    }

    // FIXME
    private void diffAll(BandDTO bandDTO) {
        log.info("diffAll({})", bandDTO);

        try {
            // get Albums from db
            List<AlbumDTO> albumsFromDB = albumService.findAllByBandId(bandDTO.getId());

            // calculate differences
            DiffResult<AlbumDTO> diffResult = diffsWeb(bandDTO.getMaKey(), albumsFromDB);
            DiscographyResult discographyResult = new DiscographyResult(bandDTO, diffResult.getChanges(), diffResult.getDiffs());

            // notify
            if (diffResult.getChanges() > 0) {
                notificationService.execute(discographyResult);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public DiffResponse diffByBandId(long bandId) {
        log.info("diffByBandId({})", bandId);

        // get Band from db
        Optional<BandDTO> bandDTOOpt = bandService.findById(bandId);
        if (bandDTOOpt.isEmpty()) {
            log.info("band with id: {} wasn't found", bandId);
            return null;
        }
        BandDTO bandDTO = bandDTOOpt.get();

        DiffResponse diffResponse = new DiffResponse();
        diffResponse.setBandDTO(bandDTO);

        // get Albums from db
        List<AlbumDTO> albums = albumService.findAllByBandId(bandDTO.getId());
        log.info("{} albums found on db for band: {}", albums.size(), bandDTO.getName());

        // FIXME search MP3 and Covers for each Albums
        mp3Service.findAndUpdate(albums);
        coversService.findAndUpdate(albums);

        // diffs from web
        diffResponse.setAlbumDiff(diffsWeb(bandDTO.getMaKey(), albums));

        // diffs covers
        diffResponse.setCoversDiff(diffsCovers(bandDTO.getName(), albums));

        // diffs mp3
        diffResponse.setMp3Diff(diffsMP3(bandDTO.getName(), albums));

        // diffs scans
        diffResponse.setScansDiff(diffsScans(bandDTO.getName(), albums));

        return diffResponse;
    }

    @SneakyThrows
    private DiffResult<AlbumDTO> diffsWeb(Long maKey, List<AlbumDTO> albums) {
        log.info("diffsWeb({}, {})", maKey, albums.size());

        // get album from web
        List<AlbumDTO> albumsFromWeb = Objects.isNull(maKey) ? Collections.emptyList() : ripperService.execute(maKey);

        // diff album
        return albumDTODiffService.execute(albums, albumsFromWeb);
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
