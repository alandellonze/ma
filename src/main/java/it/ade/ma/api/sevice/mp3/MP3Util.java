package it.ade.ma.api.sevice.mp3;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v24Tag;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.NotSupportedException;
import it.ade.ma.api.sevice.db.model.dto.AlbumDTO;
import it.ade.ma.api.sevice.path.PathUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Slf4j
@Component
@AllArgsConstructor
public class MP3Util {

    // TODO move into configuration section (or album exceptions list)
    private static final Integer MP3_TAG_DEFAULT_GENRE = 9;
    private static final String MP3_TAG_DEFAULT_GENRE_DESCRIPTION = "Metal";

    private PathUtil pathUtil;

    public ID3v2 createID3v2Template(String cdName, AlbumDTO album) throws IOException {
        // create the id3v2 template
        ID3v2 id3v2TagTemplate = new ID3v24Tag();
        id3v2TagTemplate.setArtist(album.getBandName());
        id3v2TagTemplate.setAlbum(album.getName() + (StringUtils.isNotBlank(cdName) ? (" - " + cdName) : ""));
        id3v2TagTemplate.setYear(album.getYear().toString());
        id3v2TagTemplate.setGenre(MP3_TAG_DEFAULT_GENRE);
        id3v2TagTemplate.setGenreDescription(MP3_TAG_DEFAULT_GENRE_DESCRIPTION);

        // get cover from disk
        String albumCover = pathUtil.generateCoverNameFull(album);
        byte[] albumCoverImage = Files.readAllBytes(Paths.get(albumCover));
        String albumCoverMime = "image/jpeg";
        id3v2TagTemplate.setAlbumImage(albumCoverImage, albumCoverMime);

        return id3v2TagTemplate;
    }

    public String extractTitleFromMp3File(Mp3File mp3File) {
        log.info("extractTitleFromMp3File(mp3File={})", mp3File.getFilename());

        String filename = mp3File.getFilename();
        filename = filename.substring(filename.lastIndexOf("/") + 1, filename.length());
        filename = filename.substring(filename.indexOf("-") + 1, filename.length());
        filename = filename.substring(0, filename.indexOf(".mp3"));
        return filename.trim();
    }

    public void updateMP3File(Mp3File mp3File) throws IOException, NotSupportedException {
        log.info("updateMP3File(mp3File={})", mp3File.getFilename());

        // save new tags
        String fileName = mp3File.getFilename();
        String tmpFileName = fileName + "_TMP";
        mp3File.save(tmpFileName);
        Files.move(Paths.get(tmpFileName), Paths.get(fileName), StandardCopyOption.REPLACE_EXISTING);

        // normalize and replace file's name
        if (mp3File.getId3v2Tag() != null) {
            String[] currentFileName = splitFileNameFromMp3File(mp3File);
            String newFileName = pathUtil.normalizeName(extractFileNameFromID3v2(mp3File.getId3v2Tag()));
            if (!currentFileName[1].equals(newFileName)) {
                log.info("normalize file name to: {}", newFileName);
                Files.move(Paths.get(fileName), Paths.get(tmpFileName), StandardCopyOption.REPLACE_EXISTING);
                Files.move(Paths.get(tmpFileName), Paths.get(currentFileName[0] + newFileName), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private String[] splitFileNameFromMp3File(Mp3File mp3File) {
        String filePathAndName = mp3File.getFilename();
        int i = filePathAndName.lastIndexOf("/") + 1;
        String filePath = filePathAndName.substring(0, i);
        String fileName = filePathAndName.substring(i, filePathAndName.length());
        return new String[]{filePath, fileName};
    }

    private String extractFileNameFromID3v2(ID3v2 id3v2Tag) {
        return id3v2Tag.getTrack() + " - " + id3v2Tag.getTitle() + ".mp3";
    }

}
