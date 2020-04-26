package it.ade.ma.api.sevice;

import it.ade.ma.api.model.dto.AlbumDTO;
import it.ade.ma.api.model.enums.CoverStatus;
import it.ade.ma.api.util.PathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CoverService {

    private final static Logger logger = LoggerFactory.getLogger(CoverService.class);

    private PathUtil pathUtil;

    @Autowired
    public void setPathUtil(PathUtil pathUtil) {
        this.pathUtil = pathUtil;
    }

    void findAndUpdate(List<AlbumDTO> albums) {
        logger.info("findAndUpdate({})", (albums != null ? albums.size() : null));

        albums.forEach(album -> {
            String path = pathUtil.generateCoverName(album);
            boolean exists = pathUtil.fileExists(path);
            album.setCoverStatus(exists ? CoverStatus.PRESENT : CoverStatus.NOT_PRESENT);
        });
    }

}
