package it.ade.ma.api.util;

import com.google.common.collect.Maps;
import it.ade.ma.api.exception.AlbumTypeNormalizationNotFoundException;
import it.ade.ma.api.model.Album;
import it.ade.ma.api.model.dto.WebPageAlbum;
import org.apache.commons.text.WordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class RipperService {

    @Autowired
    private WebPageContentService webPageContentService;

    @Autowired
    private AlbumTypeService albumTypeService;

    public List<Album> execute(Long bandMAKey) throws IOException {
        List<Album> albums = new ArrayList<>();

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
                e.printStackTrace();
                type = "<" + webPageAlbum.getType() + ">";
            }

            // assign type count
            Integer typeCount = albumTypeService.calculateCount(typeCounts, type);

            // capitalize name
            String name = WordUtils.capitalize(webPageAlbum.getName());

            // convert year
            Integer year = Integer.parseInt(webPageAlbum.getYear());

            // add to the list
            albums.add(new Album((i + 1), type, typeCount, name, year));
        }

        return albums;
    }

}
