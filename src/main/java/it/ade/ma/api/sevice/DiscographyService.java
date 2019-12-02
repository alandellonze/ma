package it.ade.ma.api.sevice;

import it.ade.ma.api.model.Album;
import it.ade.ma.api.model.Band;
import it.ade.ma.api.model.dto.AlbumDiff;
import it.ade.ma.api.model.dto.DiscographyResult;
import it.ade.ma.api.repository.AlbumRepository;
import it.ade.ma.api.repository.BandRepository;
import it.ade.ma.api.util.DiffService;
import it.ade.ma.api.util.RipperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiscographyService {

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
        List<Band> bands = bandRepository.findAllByMaKeyNotNullOrderByName();
        bands.forEach(band -> adjustDiscography(band.getName(), null));
    }

    public void executeAll() {
        List<Band> bands = bandRepository.findAllByMaKeyNotNullOrderByName();
        bands.forEach(band -> execute(band.getName(), true));
    }

    public DiscographyResult execute(String bandName, boolean sendNotification) {
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
                if (sendNotification) {
                    notificationService.execute(discographyResult);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return discographyResult;
    }

    public boolean plus(Band band, AlbumDiff albumDiff) {
        albumDiff.getRevised().stream().forEach(album -> {
            album.setBand(band);
            albumRepository.save(album);

            // adjust discography
            adjustDiscography(band.getName(), album.getPosition());
            adjustDiscography(band.getName(), null);
        });
        return true;
    }

    public boolean change(Band band, AlbumDiff albumDiff) {
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
        adjustDiscography(band.getName(), null);
        return true;
    }

    public boolean minus(Long albumId) {
        // get album
        Album album = albumRepository.findById(albumId).get();
        String bandName = album.getBand().getName();

        // delete album
        albumRepository.deleteById(album.getId());

        // adjust discography
        adjustDiscography(bandName, null);
        return true;
    }

    private void adjustDiscography(String bandName, Integer position) {
        // normalize inputs
        if (position == null) {
            position = 0;
        }

        // get all the Albums
        List<Album> albums = albumRepository.findAllByBandNameOrderByPositionAsc(bandName);

        // adjust position
        for (int i = position; i < albums.size(); i++) {
            Album album = albums.get(i);
            if (album.getPosition() != i + 1) {
                System.out.println(bandName + ": " + album.getPosition() + " -> " + (i + 1) + " - " + album.getName());
                album.setPosition(i + 1);
                albumRepository.save(album);
            }
        }
    }

}
