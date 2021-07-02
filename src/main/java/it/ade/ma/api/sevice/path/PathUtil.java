package it.ade.ma.api.sevice.path;

import it.ade.ma.api.sevice.db.model.dto.AlbumDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class PathUtil {

    private final PathConfiguration pathConfiguration;

    public Stream<String> getAllCovers(String bandName) throws IOException {
        String folderName = pathConfiguration.getCovers() + "/" + normalizeName(bandName);
        return getFolderContent(folderName);
    }

    public String generateCoverNameFull(AlbumDTO album) {
        return pathConfiguration.getCovers() + "/" + normalizeName(album.getBandName()) + "/" + generateCoverName(album);
    }

    public static String generateCoverName(AlbumDTO album) {
        return generateAlbumName(album) + ".jpg";
    }

    public Stream<String> getAllScans(String bandName) throws IOException {
        String folderName = pathConfiguration.getScans() + "/" + normalizeName(bandName);
        return getFolderContent(folderName);
    }

    public static String generateScanName(AlbumDTO album) {
        return generateAlbumName(album);
    }

    public Stream<String> getAllMP3s(String bandName) throws IOException {
        String folderName = pathConfiguration.getMp3() + "/" + normalizeName(bandName);
        return getFolderContent(folderName);
    }

    public String generateMP3NameFull(AlbumDTO album) {
        return pathConfiguration.getMp3() + "/" + normalizeName(album.getBandName()) + "/" + generateMP3Name(album);
    }

    public static String generateMP3Name(AlbumDTO album) {
        return generateAlbumName(album);
    }

    public String generateTMPNameFull(AlbumDTO album) {
        return pathConfiguration.getTmp() + "/" + normalizeName(album.getBandName()) + " - " + generateAlbumName(album);
    }

    public Stream<String> getAllTmpMP3s(String bandName) throws IOException {
        String normalizeBandName = normalizeName(bandName) + " - ";
        return getFolderContent(pathConfiguration.getTmp(), normalizeBandName)
                .map(p -> p.substring(normalizeBandName.length()));
    }

    private static String generateAlbumName(AlbumDTO album) {
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

    public static String normalizeName(String name) {
        name = name.replaceAll(":", " -");
        name = name.replaceAll("/", "-");
        return name;
    }

    public Map<String, List<String>> getMP3FileNameMap(AlbumDTO album) throws IOException {
        String folderName = generateMP3NameFull(album);
        if (!fileExists(folderName)) {
            folderName = generateTMPNameFull(album);
        }
        return getFileNameMap(folderName);
    }

    private Stream<String> getFolderContent(String folderName, String startWith) throws IOException {
        return getFolderContent(folderName)
                .filter(f -> f.startsWith(startWith));
    }

    private Stream<String> getFolderContent(String folderName) throws IOException {
        Path path = Paths.get(folderName);
        if (Files.exists(path)) {
            int folderLength = folderName.length();
            return Files.walk(Paths.get(folderName), 1)
                    .filter(p -> p.toString().length() > folderLength)
                    .map(p -> p.toString().substring(folderLength + 1))
                    .sorted();
        }
        return Stream.empty();
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
