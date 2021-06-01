package it.ade.ma.api.sevice.discography;

import it.ade.ma.api.sevice.covers.CoversService;
import it.ade.ma.api.sevice.db.model.Band;
import it.ade.ma.api.sevice.db.model.dto.AlbumDTO;
import it.ade.ma.api.sevice.db.repository.BandRepository;
import it.ade.ma.api.sevice.db.service.AlbumService;
import it.ade.ma.api.sevice.discography.model.AlbumDiff;
import it.ade.ma.api.sevice.discography.model.DiscographyResult;
import it.ade.ma.api.sevice.mail.NotificationService;
import it.ade.ma.api.sevice.mp3.MP3Service;
import it.ade.ma.api.sevice.ripper.RipperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscographyService {

    private final BandRepository bandRepository;
    private final AlbumService albumService;
    private final MP3Service mp3Service;
    private final CoversService coversService;
    private final RipperService ripperService;
    private final DiffService diffService;
    private final NotificationService notificationService;

    public void adjustPositions() {
        log.info("adjustPositions()");

        List<Band> bands = bandRepository.findAllByMaKeyNotNullOrderByName();
        bands.forEach(band -> albumService.adjustPositions(band.getName()));
    }

    public void executeAll() {
        log.info("executeAll()");

        List<Band> bands = bandRepository.findAllByMaKeyNotNullOrderByName();
        bands.forEach(band -> execute(band.getName(), true));
    }

    public DiscographyResult execute(String bandName, boolean sendNotification) {
        log.info("execute({}, {})", bandName, sendNotification);

        DiscographyResult discographyResult = null;
        try {
            // get Band from db
            Band band = bandRepository.findOneByName(bandName);
            if (band != null) {
                // get Albums from db
                List<AlbumDTO> albumsFromDB = albumService.findAllByBandName(band.getName());

                // search MP3 for each Albums
                mp3Service.findAndUpdate(albumsFromDB);

                // search Covers for each Albums
                coversService.findAndUpdate(albumsFromDB);

                // get Albums from web
                List<AlbumDTO> albumsFromWeb = Objects.isNull(band.getMaKey()) ? Collections.emptyList() : ripperService.execute(band.getMaKey());

                // calculate differences
                discographyResult = diffService.execute(albumsFromDB, albumsFromWeb);

                // add Band information
                discographyResult.setBand(band);

                // notify
                if (sendNotification && discographyResult.getChanges() > 0) {
                    notificationService.execute(discographyResult);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return discographyResult;
    }

    public void equal(Band band, AlbumDiff albumDiff) {
        log.info("equal({}, {})", band, albumDiff);

        // update record
        AlbumDTO album = albumDiff.getOriginal().get(0);
        if (StringUtils.isEmpty(album.getMaName())) {
            album.setMaName(null);
        }
        if (StringUtils.isEmpty(album.getMaType())) {
            album.setMaType(null);
        }
        albumService.save(album);

        // adjust discography
        albumService.adjustPositions(band.getName());
    }

    public void plus(Band band, AlbumDiff albumDiff) {
        log.info("plus({}, {})", band, albumDiff);

        // shift all the positions below the new first position and total size
        Integer start = albumDiff.getRevised().get(0).getPosition();
        Integer offset = albumDiff.getRevised().size();
        albumService.adjustPositions(band.getName(), start, offset);

        // add
        albumDiff.getRevised().forEach(album -> {
            album.setBandId(band.getId());
            albumService.save(album);
        });

        // adjust discography
        albumService.adjustPositions(band.getName());
    }

    public void change(Band band, AlbumDiff albumDiff) {
        log.info("change({}, {})", band, albumDiff);

        // shift all the positions below the new first position and total size
        Integer start = albumDiff.getRevised().get(0).getPosition() + albumDiff.getOriginal().size();
        Integer offset = Math.abs(albumDiff.getOriginal().size() - albumDiff.getRevised().size());
        albumService.adjustPositions(band.getName(), start, offset);

        int i = 0;
        for (; i < albumDiff.getOriginal().size(); i++) {
            AlbumDTO albumOriginal = albumDiff.getOriginal().get(i);
            AlbumDTO albumRevised = (i < albumDiff.getRevised().size()) ? albumDiff.getRevised().get(i) : null;

            // minus
            if (albumRevised == null) {
                albumService.delete(albumOriginal.getId());
            }
            // change
            else {
                albumOriginal.setPosition(albumRevised.getPosition());
                albumOriginal.setType(albumRevised.getType());
                albumOriginal.setTypeCount(albumRevised.getTypeCount());
                albumOriginal.setName(albumRevised.getName());
                albumOriginal.setYear(albumRevised.getYear());

                albumOriginal.setBandId(band.getId());
                albumService.save(albumOriginal);
            }
        }

        if (i < albumDiff.getRevised().size()) {
            for (; i < albumDiff.getRevised().size(); i++) {
                AlbumDTO albumRevised = albumDiff.getRevised().get(i);

                // plus
                albumRevised.setBandId(band.getId());
                albumService.save(albumRevised);
            }
        }

        // adjust discography
        albumService.adjustPositions(band.getName());
    }

    public void minus(Long albumId) {
        log.info("minus({})", albumId);

        // get album
        AlbumDTO album = albumService.findById(albumId);

        // delete album
        albumService.delete(albumId);

        // adjust discography
        albumService.adjustPositions(album.getBandName());
    }

}
