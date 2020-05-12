package it.ade.ma.api.util;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v23Tag;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.NotSupportedException;
import it.ade.ma.api.model.dto.AlbumDTO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Component
public class MP3Util {

    private final static Logger logger = LoggerFactory.getLogger(MP3Util.class);

    private PathUtil pathUtil;

    @Autowired
    public void setPathUtil(PathUtil pathUtil) {
        this.pathUtil = pathUtil;
    }

    // TODO move into configuration section (or album exceptions list)
    private static final Integer MP3_TAG_DEFAULT_GENRE = 9;
    private static final String MP3_TAG_DEFAULT_GENRE_DESCRIPTION = "Metal";

    public ID3v2 createID3v2Template(String cdName, AlbumDTO album) throws IOException {
        // create the id3v2 template
        ID3v2 id3v2TagTemplate = new ID3v23Tag();
        id3v2TagTemplate.setArtist(album.getBandName());
        id3v2TagTemplate.setAlbum(album.getName() + (StringUtils.isNotBlank(cdName) ? (" - " + cdName) : ""));
        id3v2TagTemplate.setYear(album.getYear().toString());
        id3v2TagTemplate.setGenre(MP3_TAG_DEFAULT_GENRE);
        id3v2TagTemplate.setGenreDescription(MP3_TAG_DEFAULT_GENRE_DESCRIPTION);

        // get cover from disk
        String albumCover = pathUtil.generateCoverName(album);
        byte[] albumCoverImage = Files.readAllBytes(Paths.get(albumCover));
        String albumCoverMime = "image/jpeg";
        id3v2TagTemplate.setAlbumImage(albumCoverImage, albumCoverMime);

        return id3v2TagTemplate;
    }

    public String extractTitleFromFileName(Mp3File mp3File) {
        logger.info("extractTitleFromFileName(mp3File={})", mp3File.getFilename());

        String filename = mp3File.getFilename();
        filename = filename.substring(filename.lastIndexOf("/") + 1, filename.length());
        filename = filename.substring(filename.indexOf("-") + 1, filename.length());
        filename = filename.substring(0, filename.indexOf(".mp3"));
        return filename.trim();
    }

    public void updateMP3File(Mp3File mp3File) throws IOException, NotSupportedException {
        logger.info("updateMP3File(mp3File={})", mp3File.getFilename());

        mp3File.save(mp3File.getFilename() + "_TMP");
        Files.move(Paths.get(mp3File.getFilename() + "_TMP"), Paths.get(mp3File.getFilename()), StandardCopyOption.REPLACE_EXISTING);
    }

}
