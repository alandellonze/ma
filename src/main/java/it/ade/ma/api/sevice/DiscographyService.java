package it.ade.ma.api.sevice;

import it.ade.ma.api.model.dto.AlbumDTO;
import it.ade.ma.api.model.dto.AlbumDiff;
import it.ade.ma.api.model.dto.DiscographyResult;
import it.ade.ma.api.model.entity.Band;
import it.ade.ma.api.repository.BandRepository;
import it.ade.ma.api.util.DiffService;
import it.ade.ma.api.util.RipperService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class DiscographyService {

    private final static Logger logger = LoggerFactory.getLogger(DiscographyService.class);

    private BandRepository bandRepository;
    private AlbumService albumService;
    private MP3Service mp3Service;
    private CoverService coverService;
    private RipperService ripperService;
    private DiffService diffService;
    private NotificationService notificationService;

    public void adjustPositions() {
        logger.info("adjustPositions()");

        List<Band> bands = bandRepository.findAllByMaKeyNotNullOrderByName();
        bands.forEach(band -> albumService.adjustPositions(band.getName()));
    }

    public void executeAll() {
        logger.info("executeAll()");

        List<Band> bands = bandRepository.findAllByMaKeyNotNullOrderByName();
        bands.forEach(band -> execute(band.getName(), true));
    }

    public DiscographyResult execute(String bandName, boolean sendNotification) {
        logger.info("execute({}, {})", bandName, sendNotification);

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
                coverService.findAndUpdate(albumsFromDB);

                // get Albums from web
                List<AlbumDTO> albumsFromWeb = ripperService.execute(band.getMaKey());

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
            logger.error(e.getMessage(), e);
        }
        return discographyResult;
    }

    public void equal(Band band, AlbumDiff albumDiff) {
        logger.info("equal({}, {})", band, albumDiff);

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
        logger.info("plus({}, {})", band, albumDiff);

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
        logger.info("change({}, {})", band, albumDiff);

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
        logger.info("minus({})", albumId);

        // get album
        AlbumDTO album = albumService.findById(albumId);

        // delete album
        albumService.delete(albumId);

        // adjust discography
        albumService.adjustPositions(album.getBandName());
    }

}
