package it.ade.ma.api.sevice.covers;

import it.ade.ma.api.constants.CoverStatus;
import it.ade.ma.api.sevice.db.model.dto.AlbumDTO;
import it.ade.ma.api.sevice.path.PathUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoversService {

    private final PathUtil pathUtil;

    @Deprecated
    public void findAndUpdate(List<AlbumDTO> albums) {
        log.info("findAndUpdate({})", (albums != null ? albums.size() : null));

        if (albums != null) {
            albums.forEach(album -> {
                String path = pathUtil.generateCoverNameFull(album);
                boolean exists = pathUtil.fileExists(path);
                album.setCoverStatus(exists ? CoverStatus.PRESENT : CoverStatus.NOT_PRESENT);
            });
        }
    }

    public List<String> getAllCovers(String bandName) throws IOException {
        return pathUtil.getAllCovers(bandName)
                .collect(Collectors.toList());
    }

    public List<String> getAllScans(String bandName) throws IOException {
        return pathUtil.getAllScans(bandName)
                .collect(Collectors.toList());
    }

}
