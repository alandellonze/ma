package it.ade.ma.api.util;

import it.ade.ma.api.model.dto.AlbumDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        StringBuilder folderName = new StringBuilder(maPathMain).append(maPathTMP);

        // add band folder
        folderName.append(album.getBandName()).append(" - ");

        folderName.append(generateAlbumName(album));
        return folderName.toString();
    }

    // FIXME cover image: it couldn't be a jpg image...
    public String generateCoverName(AlbumDTO album) {
        StringBuilder folderName = new StringBuilder(maPathMain).append(maPathCover);

        // add band folder
        folderName.append(album.getBandName()).append("/");

        folderName.append(generateAlbumName(album)).append(".jpg");
        return folderName.toString();
    }

    public String generateMP3Name(AlbumDTO album) {
        StringBuilder folderName = new StringBuilder(maPathMain).append(maPathMP3);

        // add band folder
        folderName.append(album.getBandName()).append("/");

        folderName.append(generateAlbumName(album));
        return folderName.toString();
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
        folderName.append(" - ").append(name);

        return folderName.toString();
    }

    public List<String> getMP3FileNameList(AlbumDTO album) throws IOException {
        String albumFolder = generateMP3Name(album);
        return getFileList(albumFolder);
    }

    List<String> getFileList(String folder) throws IOException {
        List<String> fileList = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(folder))) {
            for (Path path : stream) {
                if (!Files.isDirectory(path)) {
                    fileList.add(folder + "/" + path.getFileName().toString());
                }
            }
        }

        Collections.sort(fileList);
        return fileList;
    }

    public boolean fileExists(String path) {
        return Files.exists(Paths.get(path));
    }

}
