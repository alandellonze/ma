package it.ade.ma.api.sevice.discography;

import it.ade.ma.api.sevice.db.model.dto.AlbumDTO;
import it.ade.ma.api.sevice.db.service.AlbumService;
import it.ade.ma.api.sevice.diff.engine.model.DiffRow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscographyService {

    private final AlbumService albumService;

    public void equal(long bandId, DiffRow<AlbumDTO> diff) {
        log.info("equal({}, {})", bandId, diff);

        // update record
        AlbumDTO album = diff.getOriginal().get(0);
        if (StringUtils.isEmpty(album.getMaType())) {
            album.setMaType(null);
        }
        if (StringUtils.isEmpty(album.getMaName())) {
            album.setMaName(null);
        }
        albumService.save(album);

        // adjust discography
        albumService.adjustPositions(bandId);
    }

    public void plus(long bandId, DiffRow<AlbumDTO> diff) {
        log.info("plus({}, {})", bandId, diff);

        // shift all the positions below the new first position and total size
        int start = diff.getRevised().get(0).getPosition();
        int offset = diff.getRevised().size();
        albumService.adjustPositions(bandId, start, offset);

        // add
        diff.getRevised().forEach(album -> {
            album.setBandId(bandId);
            albumService.save(album);
        });

        // adjust discography
        albumService.adjustPositions(bandId);
    }

    public void change(long bandId, DiffRow<AlbumDTO> diff) {
        log.info("change({}, {})", bandId, diff);

        // shift all the positions below the new first position and total size
        Integer start = diff.getRevised().get(0).getPosition() + diff.getOriginal().size();
        Integer offset = Math.abs(diff.getOriginal().size() - diff.getRevised().size());
        albumService.adjustPositions(bandId, start, offset);

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

                albumOriginal.setBandId(bandId);
                albumService.save(albumOriginal);
            }
        }

        if (i < diff.getRevised().size()) {
            for (; i < diff.getRevised().size(); i++) {
                AlbumDTO albumRevised = diff.getRevised().get(i);

                // plus
                albumRevised.setBandId(bandId);
                albumService.save(albumRevised);
            }
        }

        // adjust discography
        albumService.adjustPositions(bandId);
    }

    public void minus(long bandId, long albumId) {
        log.info("minus({}, {})", bandId, albumId);

        // delete album
        albumService.delete(albumId);

        // adjust discography
        albumService.adjustPositions(bandId);
    }

}
