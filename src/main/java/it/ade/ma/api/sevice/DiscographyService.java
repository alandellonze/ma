package it.ade.ma.api.sevice;

import it.ade.ma.api.model.Album;
import it.ade.ma.api.model.Band;
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

    public void executeAll() {
        List<Band> bands = bandRepository.findAllByMaKeyNotNullOrderByName();
        bands.forEach(band -> execute(band.getName()));
    }

    public DiscographyResult execute(String bandName) {
        DiscographyResult discographyResult = null;
        try {
            // get Band from db
            Band band = bandRepository.findOneByName(bandName);

            // get Albums from db
            List<Album> albumsFromDB = albumRepository.findAllByBandNameOrderByPositionAsc(band.getName());

            // get Albums from web
            List<Album> albumsFromWeb = ripperService.execute(band.getMaKey());

            // calculate differences
            discographyResult = diffService.execute(albumsFromDB, albumsFromWeb);

            // add Band information
            discographyResult.setBand(band);

            // notify
            notificationService.execute(discographyResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return discographyResult;
    }

}
