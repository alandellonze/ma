package it.ade.ma.api.sevice;

import it.ade.ma.api.model.dto.AlbumDTO;
import it.ade.ma.api.model.enums.CoverStatus;
import it.ade.ma.api.util.PathUtil;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CoverService {

    private final static Logger logger = LoggerFactory.getLogger(CoverService.class);

    private PathUtil pathUtil;

    void findAndUpdate(List<AlbumDTO> albums) {
        logger.info("findAndUpdate({})", (albums != null ? albums.size() : null));

        if (albums != null) {
            albums.forEach(album -> {
                String path = pathUtil.generateCoverName(album);
                boolean exists = pathUtil.fileExists(path);
                album.setCoverStatus(exists ? CoverStatus.PRESENT : CoverStatus.NOT_PRESENT);
            });
        }
    }

}
