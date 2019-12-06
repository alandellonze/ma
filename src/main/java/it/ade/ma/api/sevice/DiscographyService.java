package it.ade.ma.api.sevice;

import it.ade.ma.api.model.Album;
import it.ade.ma.api.model.Band;
import it.ade.ma.api.model.dto.AlbumDiff;
import it.ade.ma.api.model.dto.DiscographyResult;
import it.ade.ma.api.repository.AlbumRepository;
import it.ade.ma.api.repository.BandRepository;
import it.ade.ma.api.util.DiffService;
import it.ade.ma.api.util.RipperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiscographyService {

    private final static Logger logger = LoggerFactory.getLogger(DiscographyService.class);

    @Autowired
    private BandRepository bandRepository;

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private RipperService ripperService;

    @Autowired
    private DiffService diffService;

    @Autowired
    private NotificationService notificationService;

    public void adjustAllPositions() {
        logger.info("adjustAllPositions()");

        List<Band> bands = bandRepository.findAllByMaKeyNotNullOrderByName();
        bands.forEach(band -> adjustDiscography(band.getName()));
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
                List<Album> albumsFromDB = albumRepository.findAllByBandNameOrderByPositionAsc(band.getName());

                // get Albums from web
                List<Album> albumsFromWeb = ripperService.execute(band.getMaKey());

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

    public void plus(Band band, AlbumDiff albumDiff) {
        logger.info("plus({}, {})", band, albumDiff);

        // shift all the positions below the new first position and total size
        Integer start = albumDiff.getRevised().get(0).getPosition();
        Integer offset = albumDiff.getRevised().size();
        adjustDiscography(band.getName(), start, offset);

        // add
        albumDiff.getRevised().stream().forEach(album -> {
            album.setBand(band);
            albumRepository.save(album);
        });

        // adjust discography
        adjustDiscography(band.getName());
    }

    public void change(Band band, AlbumDiff albumDiff) {
        logger.info("change({}, {})", band, albumDiff);

        // shift all the positions below the new first position and total size
        Integer start = albumDiff.getRevised().get(0).getPosition() + albumDiff.getOriginal().size();
        Integer offset = Math.abs(albumDiff.getOriginal().size() - albumDiff.getRevised().size());
        adjustDiscography(band.getName(), start, offset);

        int i = 0;
        for (; i < albumDiff.getOriginal().size(); i++) {
            Album albumOriginal = albumDiff.getOriginal().get(i);
            Album albumRevised = (i < albumDiff.getRevised().size()) ? albumDiff.getRevised().get(i) : null;

            // minus
            if (albumRevised == null) {
                albumRepository.deleteById(albumOriginal.getId());
            }
            // change
            else {
                albumOriginal.setPosition(albumRevised.getPosition());
                albumOriginal.setType(albumRevised.getType());
                albumOriginal.setTypeCount(albumRevised.getTypeCount());
                albumOriginal.setName(albumRevised.getName());
                albumOriginal.setYear(albumRevised.getYear());

                albumOriginal.setBand(band);
                albumRepository.save(albumOriginal);
            }
        }

        if (i < albumDiff.getRevised().size()) {
            for (; i < albumDiff.getRevised().size(); i++) {
                Album albumRevised = albumDiff.getRevised().get(i);

                // plus
                albumRevised.setBand(band);
                albumRepository.save(albumRevised);
            }
        }

        // adjust discography
        adjustDiscography(band.getName());
    }

    public void minus(Long albumId) {
        logger.info("minus({})", albumId);

        // get album
        Album album = albumRepository.findById(albumId).get();
        String bandName = album.getBand().getName();

        // delete album
        albumRepository.deleteById(album.getId());

        // adjust discography
        adjustDiscography(bandName);
    }

    private void adjustDiscography(String bandName) {
        adjustDiscography(bandName, 1, 0);
    }

    private void adjustDiscography(String bandName, Integer start, Integer offset) {
        logger.info("adjustDiscography({}, {}, {})", bandName, start, offset);

        // get all the Albums
        List<Album> albums = albumRepository.findAllByBandNameOrderByPositionAsc(bandName);

        // adjust position
        for (int i = start - 1; i < albums.size(); i++) {
            Album album = albums.get(i);
            Integer position = i + 1 + offset;
            if (!position.equals(album.getPosition())) {
                logger.debug("{}, {}: {} -> {} - {}", bandName, i, album.getPosition(), position, album.getName());
                album.setPosition(position);
                albumRepository.save(album);
            }
        }
    }

}
