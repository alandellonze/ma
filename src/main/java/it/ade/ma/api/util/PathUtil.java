package it.ade.ma.api.util;

import it.ade.ma.api.model.dto.AlbumDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Component
public class PathUtil {

    @Value("${ma.path.main}")
    private String maPathMain;

    @Value("${ma.path.tmp}")
    private String maPathTMP;

    @Value("${ma.path.cover}")
    private String maPathCover;

    @Value("${ma.path.mp3}")
    private String maPathMP3;

    public String generateTMPName(AlbumDTO album) {
        return maPathMain + maPathTMP + normalizeName(album.getBandName()) + " - " + generateAlbumName(album);
    }

    // FIXME cover image: it couldn't be a jpg image...
    public String generateCoverName(AlbumDTO album) {
        return maPathMain + maPathCover + normalizeName(album.getBandName()) + "/" + generateAlbumName(album) + ".jpg";
    }

    public String generateMP3Name(AlbumDTO album) {
        return maPathMain + maPathMP3 + normalizeName(album.getBandName()) + "/" + generateAlbumName(album);
    }

    private String generateAlbumName(AlbumDTO album) {
        // add album type
        String type = (album.getMaType() != null) ? album.getMaType() : album.getType();
        type = (type == null || "FULLLENGTH".equals(type)) ? "" : type;
        StringBuilder folderName = new StringBuilder(type);

        // add album typeCount
        Integer typeCount = (album.getMaTypeCount() != null) ? album.getMaTypeCount() : album.getTypeCount();
        folderName.append(String.format("%02d", typeCount));

        // add name
        String name = (album.getMaName() != null) ? album.getMaName() : album.getName();
        folderName.append(" - ").append(normalizeName(name));

        return folderName.toString();
    }

    public String normalizeName(String name) {
        name = name.replaceAll(":", " -");
        name = name.replaceAll("/", "-");
        return name;
    }

    public Map<String, List<String>> getMP3FileNameMap(AlbumDTO album) throws IOException {
        String folderName = generateMP3Name(album);
        if (!fileExists(folderName)) {
            folderName = generateTMPName(album);
        }
        return getFileNameMap(folderName);
    }

    private Map<String, List<String>> getFileNameMap(String folderName) throws IOException {
        Map<String, List<String>> fileMap = new LinkedHashMap<>();

        Files.walk(Paths.get(folderName))
                // FIXME put - flac and .mp3 in configurations
                .filter(path -> (!path.toString().contains("- flac") && path.toString().endsWith(".mp3")))
                .sorted()
                .forEach(path -> {
                    String cd = null;

                    // get the sub folder (if exists)
                    int folderNamePartsSize = folderName.split("/").length;
                    List<String> dirParts = Arrays.asList(path.toString().split("/"));
                    if (folderNamePartsSize < dirParts.size() - 1) {
                        cd = String.join(" - ", dirParts.subList(folderNamePartsSize, dirParts.size() - 1));
                    }

                    // insert the mp3 name grouped by sub folders
                    List<String> files = fileMap.computeIfAbsent(cd, f -> new LinkedList<>());
                    files.add(path.toString());
                });

        return fileMap;
    }


    public boolean fileExists(String path) {
        return Files.exists(Paths.get(path));
    }

}
