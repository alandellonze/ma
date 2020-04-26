package it.ade.ma.api.util;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v24Tag;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.NotSupportedException;
import it.ade.ma.api.model.dto.AlbumDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Component
public class MP3Util {

    private final static Logger logger = LoggerFactory.getLogger(MP3Util.class);

    private PathUtil pathUtil;

    @Autowired
    public void setPathUtil(PathUtil pathUtil) {
        this.pathUtil = pathUtil;
    }

    private Integer defaultGenre = 9;
    private String defaultGenreDescription = "Metal";

    public ID3v2 createID3v2Template(AlbumDTO album) throws IOException {
        // get cover from disk
        String albumCover = pathUtil.generateCoverName(album);
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
