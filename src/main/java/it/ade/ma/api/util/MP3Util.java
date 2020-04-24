package it.ade.ma.api.util;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v24Tag;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.NotSupportedException;
import it.ade.ma.api.model.dto.AlbumDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class MP3Util {

    private final static Logger logger = LoggerFactory.getLogger(MP3Util.class);

    // FIXME this data should be taken from properties
    private String rootFolder = "/Users/ade/Downloads/xxx/";
    private String mp3Folder = "mp3/";
    private String coversFolder = "covers/";

    private Integer defaultGenre = 9;
    private String defaultGenreDescription = "Metal";

    public ID3v2 createID3v2Template(AlbumDTO album) throws IOException {
        // FIXME cover image: it couldn't be a jpg image...
        // get cover from disk
        String albumCover = generateFolderName(coversFolder, album).concat(".jpg");
        byte[] albumCoverImage = Files.readAllBytes(Paths.get(albumCover));
        String albumCoverMime = "image/jpeg";

        // create the id3v2 template
        ID3v2 id3v2TagTemplate = new ID3v24Tag();
        id3v2TagTemplate.setArtist(album.getBandName());
        id3v2TagTemplate.setAlbum(album.getName());
        id3v2TagTemplate.setYear(album.getYear().toString());
        id3v2TagTemplate.setGenre(defaultGenre);
        id3v2TagTemplate.setGenreDescription(defaultGenreDescription);
        id3v2TagTemplate.setAlbumImage(albumCoverImage, albumCoverMime);

        return id3v2TagTemplate;
    }

    public List<String> getMP3FileNameList(AlbumDTO album) throws IOException {
        String albumFolder = generateFolderName(mp3Folder, album);
        return getFileList(albumFolder);
    }

    private String generateFolderName(String subFolder, AlbumDTO album) {
        // add root folders
        StringBuilder folderName = new StringBuilder(rootFolder).append(subFolder);

        // add band folder
        folderName.append(album.getBandName()).append("/");

        // add album type
        String type = (album.getMaType() != null) ? album.getMaType() : album.getType();
        type = (type == null || "FULLLENGTH".equals(type)) ? "" : type;
        folderName.append(type);

        // add album typeCount
        Integer typeCount = (album.getMaTypeCount() != null) ? album.getMaTypeCount() : album.getTypeCount();
        folderName.append(String.format("%02d", typeCount));

        // add name
        String name = (album.getMaName() != null) ? album.getMaName() : album.getName();
        folderName.append(" - ").append(name);

        return folderName.toString();
    }

    private List<String> getFileList(String folder) throws IOException {
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

    public String extractTitleFromFilaName(Mp3File mp3File) {
        logger.info("extractTitleFromFilaName(mp3File={})", mp3File.getFilename());

        String filename = mp3File.getFilename();
        filename = filename.substring(filename.lastIndexOf("/"), filename.length());
        filename = filename.substring(filename.indexOf("-"), filename.length());
        filename = filename.substring(filename.indexOf(".mp3"));
        return filename.trim();
    }

    public void updateMP3File(Mp3File mp3File) throws IOException, NotSupportedException {
        logger.info("updateMP3File(mp3File={})", mp3File.getFilename());

        mp3File.save(mp3File.getFilename() + "_TMP");
        Files.move(Paths.get(mp3File.getFilename() + "_TMP"), Paths.get(mp3File.getFilename()), StandardCopyOption.REPLACE_EXISTING);
    }

}
