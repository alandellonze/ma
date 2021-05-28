package it.ade.ma.api.sevice.covers;

import it.ade.ma.api.constants.CoverStatus;
import it.ade.ma.api.sevice.db.model.dto.AlbumDTO;
import it.ade.ma.api.sevice.path.PathUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoversService {

    private final PathUtil pathUtil;

    public void findAndUpdate(List<AlbumDTO> albums) {
        log.info("findAndUpdate({})", (albums != null ? albums.size() : null));

        if (albums != null) {
            albums.forEach(album -> {
                String path = pathUtil.generateCoverName(album);
                boolean exists = pathUtil.fileExists(path);
                album.setCoverStatus(exists ? CoverStatus.PRESENT : CoverStatus.NOT_PRESENT);
            });
        }
    }

}
