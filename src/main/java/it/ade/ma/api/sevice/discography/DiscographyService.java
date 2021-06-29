package it.ade.ma.api.sevice.discography;

import it.ade.ma.api.sevice.covers.CoversService;
import it.ade.ma.api.sevice.db.model.Band;
import it.ade.ma.api.sevice.db.model.dto.AlbumDTO;
import it.ade.ma.api.sevice.db.repository.BandRepository;
import it.ade.ma.api.sevice.db.service.AlbumService;
import it.ade.ma.api.sevice.diff.model.DiffResult;
import it.ade.ma.api.sevice.diff.model.DiffRow;
import it.ade.ma.api.sevice.discography.model.DiscographyResult;
import it.ade.ma.api.sevice.mail.NotificationService;
import it.ade.ma.api.sevice.mp3.MP3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscographyService {

    private final BandRepository bandRepository;
    private final AlbumService albumService;

    private final DiffService diffService;
    private final CoversService coversService;
    private final MP3Service mp3Service;
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

    @Deprecated
    public DiscographyResult execute(String bandName, boolean sendNotification) {
        log.info("execute({}, {})", bandName, sendNotification);

        try {
            // get Band from db
            Optional<Band> bandOpt = bandRepository.findByName(bandName);
            if (bandOpt.isPresent()) {
                Band band = bandOpt.get();

                // get Albums from db
                List<AlbumDTO> albumsFromDB = albumService.findAllByBandName(band.getName());

                // search MP3 for each Albums
                mp3Service.findAndUpdate(albumsFromDB);

                // search Covers for each Albums
                coversService.findAndUpdate(albumsFromDB);

                // calculate differences
                DiffResult<AlbumDTO> diffResult = diffService.diffsWeb(band, albumsFromDB);
                DiscographyResult discographyResult = new DiscographyResult(band, diffResult.getChanges(), diffResult.getDiffs());

                // notify
                if (sendNotification && diffResult.getChanges() > 0) {
                    notificationService.execute(discographyResult);
                }

                return discographyResult;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public void equal(Band band, DiffRow<AlbumDTO> diff) {
        log.info("equal({}, {})", band, diff);

        // update record
        AlbumDTO album = diff.getOriginal().get(0);
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

    public void plus(Band band, DiffRow<AlbumDTO> diff) {
        log.info("plus({}, {})", band, diff);

        // shift all the positions below the new first position and total size
        Integer start = diff.getRevised().get(0).getPosition();
        Integer offset = diff.getRevised().size();
        albumService.adjustPositions(band.getName(), start, offset);

        // add
        diff.getRevised().forEach(album -> {
            album.setBandId(band.getId());
            albumService.save(album);
        });

        // adjust discography
        albumService.adjustPositions(band.getName());
    }

    public void change(Band band, DiffRow<AlbumDTO> diff) {
        log.info("change({}, {})", band, diff);

        // shift all the positions below the new first position and total size
        Integer start = diff.getRevised().get(0).getPosition() + diff.getOriginal().size();
        Integer offset = Math.abs(diff.getOriginal().size() - diff.getRevised().size());
        albumService.adjustPositions(band.getName(), start, offset);

        int i = 0;
        for (; i < diff.getOriginal().size(); i++) {
            AlbumDTO albumOriginal = diff.getOriginal().get(i);
            AlbumDTO albumRevised = (i < diff.getRevised().size()) ? diff.getRevised().get(i) : null;

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

        if (i < diff.getRevised().size()) {
            for (; i < diff.getRevised().size(); i++) {
                AlbumDTO albumRevised = diff.getRevised().get(i);

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
