package it.ade.ma.api.sevice.ripper;

import com.google.common.collect.Maps;
import it.ade.ma.api.sevice.db.model.dto.AlbumDTO;
import it.ade.ma.api.sevice.ripper.exception.AlbumTypeNormalizationNotFoundException;
import it.ade.ma.api.sevice.ripper.model.WebPageAlbum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.WordUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RipperService {

    private final AlbumTypeService albumTypeService;
    private final WebPageContentService webPageContentService;

    public List<AlbumDTO> execute(long bandMAKey) throws IOException {
        log.info("execute({})", bandMAKey);

        List<AlbumDTO> albums = new ArrayList<>();

        // retrieve the album list from the web page
        List<WebPageAlbum> webPageAlbums = webPageContentService.parse(bandMAKey);

        // keep the types count
        Map<String, Integer> typeCounts = Maps.newHashMap();

        // convert WebPageAlbum into Album
        for (int i = 0; i < webPageAlbums.size(); i++) {
            WebPageAlbum webPageAlbum = webPageAlbums.get(i);

            // normalize type
            String type;
            try {
                type = albumTypeService.normalize(webPageAlbum.getType());
            } catch (AlbumTypeNormalizationNotFoundException e) {
                // FIXME album type normalization: collect all the not found normalizations in order to send a notification
                log.error(e.getMessage(), e);
                type = "<" + webPageAlbum.getType() + ">";
            }

            // assign type count
            Integer typeCount = albumTypeService.calculateCount(typeCounts, type);

            // capitalize name
            String name = WordUtils.capitalize(webPageAlbum.getName());

            // convert year
            Integer year = Integer.parseInt(webPageAlbum.getYear());

            // add to the list
            albums.add(new AlbumDTO((i + 1), type, typeCount, name, year));
        }

        return albums;
    }

}
