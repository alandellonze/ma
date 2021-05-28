package it.ade.ma.api.sevice.ripper;

import com.google.common.collect.ImmutableMap;
import it.ade.ma.api.sevice.ripper.exception.AlbumTypeNormalizationNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AlbumTypeService {

    // FIXME externalize config
    private Map<String, String> types = ImmutableMap.<String, String>builder()
            .put("Full-length", "FULLLENGTH")

            .put("Boxed set", "BOXSET")

            .put("EP", "EP")
            .put("Single", "EP")

            .put("Demo", "DEMO")

            .put("Live album", "LIVE")

            .put("Video", "VIDEO")
            .put("Video/VHS (legacy)", "VIDEO")

            .put("Compilation", "COLLECTION")

            .put("Split", "SPLIT")
            .put("Split video", "SPLIT")
            .put("Split album", "SPLIT")
            .put("Split album (legacy)", "SPLIT")
            .put("Collaboration", "SPLIT")

            .put("Covers", "COVERS")

            .put("Remastered", "REMASTERED")

            .put("Bootlegs", "BOOTLEGS")

            .put("Miscellaneous", "MISCELLANEOUS")

            .build();

    String normalize(String type) throws AlbumTypeNormalizationNotFoundException {
        String typeNormalized = types.get(type);

        if (typeNormalized == null) {
            String message = String.format("album type normalization not found: %s", type);
            throw new AlbumTypeNormalizationNotFoundException(message);
        }

        return typeNormalized;
    }

    Integer calculateCount(Map<String, Integer> typeCounts, String type) {
        Integer typeCount = typeCounts.get(type);
        if (typeCount == null) {
            typeCount = 0;
        }
        typeCounts.put(type, ++typeCount);
        return typeCount;
    }

}
