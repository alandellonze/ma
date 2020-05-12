package it.ade.ma.api.sevice;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;
import it.ade.ma.api.model.dto.AlbumDTO;
import it.ade.ma.api.model.enums.MP3Status;
import it.ade.ma.api.util.MP3Util;
import it.ade.ma.api.util.PathUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MP3Service {

    private final static Logger logger = LoggerFactory.getLogger(MP3Service.class);

    private AlbumService albumService;
    private PathUtil pathUtil;
    private MP3Util mp3Util;

    @Autowired
    public void setAlbumService(AlbumService albumService) {
        this.albumService = albumService;
    }

    @Autowired
    public void setPathUtil(PathUtil pathUtil) {
        this.pathUtil = pathUtil;
    }

    @Autowired
    public void setMp3Util(MP3Util mp3Util) {
        this.mp3Util = mp3Util;
    }

    void findAndUpdate(List<AlbumDTO> albums) {
        logger.info("findAndUpdate({})", (albums != null ? albums.size() : null));

        if (albums != null) {
            albums.forEach(album -> {
                MP3Status status;

                // look into mp3 folder
                String path = pathUtil.generateMP3Name(album);
                boolean exists = pathUtil.fileExists(path);
                if (exists) {
                    status = MP3Status.PRESENT;
                }

                // look into tmp folder
                else {
                    path = pathUtil.generateTMPName(album);
                    exists = pathUtil.fileExists(path);
                    status = exists ? MP3Status.TMP : MP3Status.NOT_PRESENT;
                }

                // update status
                album.setMp3Status(status);
            });
        }
    }

    public void check(Long albumId) throws Exception {
        logger.info("check({})", albumId);

        // get the Album
        AlbumDTO album = albumService.findById(albumId);

        // adjust all the mp3 files
        Map<String, List<String>> mp3FileNameMap = pathUtil.getMP3FileNameMap(album);
        logger.info("found {} cd for '{}'", mp3FileNameMap.size(), album);
        for (Map.Entry<String, List<String>> entry : mp3FileNameMap.entrySet()) {
            // create the id3v2 template
            String cdName = entry.getKey();
            ID3v2 id3v2TagTemplate = mp3Util.createID3v2Template(cdName, album);

            // handle files
            List<String> mp3FileNames = entry.getValue();
            logger.info("found {} files for '{}'", mp3FileNames.size(), id3v2TagTemplate.getAlbum());
            for (int i = 0; i < mp3FileNames.size(); i++) {
                handleMp3(mp3FileNames.get(i), i + 1, id3v2TagTemplate);
            }
        }
    }

    private void handleMp3(String mp3FileName, int position, ID3v2 id3v2TagTemplate) {
        logger.info("handleMp3({}, {}, id3v2TagTemplate)", mp3FileName, position);

        try {
            Mp3File mp3File = new Mp3File(mp3FileName);

            // id3v1 tag
            handleID3v1Tag(mp3File);

            // id3v2 tag
            handleID3v2Tag(mp3File, position, id3v2TagTemplate);

            // custom tag
            handleCustomTag(mp3File);

            // FIXME normalize file name too

            // save
            mp3Util.updateMP3File(mp3File);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void handleID3v1Tag(Mp3File mp3File) {
        logger.debug("handleID3v1Tag(mp3File={})", mp3File.getFilename());

        if (mp3File.hasId3v1Tag()) {
            ID3v1 id3v1Tag = mp3File.getId3v1Tag();
            logger.debug("Artist: {}", id3v1Tag.getArtist());
            logger.debug("Album: {}", id3v1Tag.getAlbum());
            logger.debug("Year: {}", id3v1Tag.getYear());
            logger.debug("Genre: {} ({})", id3v1Tag.getGenre(), id3v1Tag.getGenreDescription());
            logger.debug("Track: {}", id3v1Tag.getTrack());
            logger.debug("Title: {}", id3v1Tag.getTitle());
            logger.debug("Comment: {}", id3v1Tag.getComment());

            mp3File.removeId3v1Tag();
            logger.info("v1 tag removed");
        }
    }

    private void handleID3v2Tag(Mp3File mp3File, int position, ID3v2 id3v2TagTemplate) {
        logger.debug("handleID3v2Tag(mp3File={}, {}, id3v2TagTemplate)", mp3File.getFilename(), position);

        ID3v2 id3v2Tag = null;
        if (mp3File.hasId3v2Tag()) {
            id3v2Tag = mp3File.getId3v2Tag();
            logger.debug("Artist: {}", id3v2Tag.getArtist());
            logger.debug("Album: {}", id3v2Tag.getAlbum());
            logger.debug("Year: {}", id3v2Tag.getYear());
            logger.debug("Genre: {} ({})", id3v2Tag.getGenre(), id3v2Tag.getGenreDescription());
            byte[] albumImageData = id3v2Tag.getAlbumImage();
            if (albumImageData != null) {
                logger.debug("Album image, length: {} bytes, mime type: {}", albumImageData.length, id3v2Tag.getAlbumImageMimeType());
            }
            logger.debug("Track: {}", id3v2Tag.getTrack());
            logger.debug("Title: {}", id3v2Tag.getTitle());
            logger.debug("Album artist: {}", id3v2Tag.getAlbumArtist());
            logger.debug("Comment: {}", id3v2Tag.getComment());
            logger.debug("Composer: {}", id3v2Tag.getComposer());
            logger.debug("Copyright: {}", id3v2Tag.getCopyright());
            logger.debug("Encoder: {}", id3v2Tag.getEncoder());
            logger.debug("Lyrics: {}", id3v2Tag.getLyrics());
            logger.debug("Original artist: {}", id3v2Tag.getOriginalArtist());
            logger.debug("Publisher: {}", id3v2Tag.getPublisher());
            logger.debug("URL: {}", id3v2Tag.getUrl());

            // log changes to be applied
            logID3v2TagFieldsToBeChanged(id3v2Tag, id3v2TagTemplate);
        }

        // handle track
        handleID3v2TagTrack(id3v2TagTemplate, position);

        // handle title
        handleID3v2TagTitle(mp3File, id3v2TagTemplate, id3v2Tag);

        // change the id3v2 tag
        mp3File.removeId3v2Tag();
        mp3File.setId3v2Tag(id3v2TagTemplate);
    }

    private void logID3v2TagFieldsToBeChanged(ID3v2 id3v2Tag, ID3v2 id3v2TagTemplate) {
        if (id3v2Tag.getArtist() == null || !id3v2Tag.getArtist().equals(id3v2TagTemplate.getArtist())) {
            logger.info("Artist to be changed: {} - {}", id3v2Tag.getArtist(), id3v2TagTemplate.getArtist());
        }

        if (id3v2Tag.getAlbum() == null || !id3v2Tag.getAlbum().equals(id3v2TagTemplate.getAlbum())) {
            logger.info("Album to be changed: {} - {}", id3v2Tag.getAlbum(), id3v2TagTemplate.getAlbum());
        }

        if (id3v2Tag.getYear() == null || !id3v2Tag.getYear().equals(id3v2TagTemplate.getYear())) {
            logger.info("Year to be changed: {} - {}", id3v2Tag.getYear(), id3v2TagTemplate.getYear());
        }

        if (id3v2Tag.getGenre() != id3v2TagTemplate.getGenre()) {
            logger.info("Genre to be changed: {} - {}", id3v2Tag.getGenre(), id3v2TagTemplate.getGenre());
        }

        if (id3v2Tag.getGenreDescription() == null || !id3v2Tag.getGenreDescription().equals(id3v2TagTemplate.getGenreDescription())) {
            logger.info("GenreDescription to be changed: {} - {}", id3v2Tag.getGenreDescription(), id3v2TagTemplate.getGenreDescription());
        }

        byte[] albumImageData = id3v2Tag.getAlbumImage();
        byte[] albumImageDataTemplate = id3v2TagTemplate.getAlbumImage();
        if (albumImageData != null) {
            if (albumImageDataTemplate != null) {
                if (albumImageData.length != albumImageDataTemplate.length || !id3v2Tag.getAlbumImageMimeType().equals(id3v2TagTemplate.getAlbumImageMimeType())) {
                    logger.info("AlbumImage to be changed: {}, {} - {}, {}", id3v2Tag.getAlbumImage().length, id3v2Tag.getAlbumImageMimeType(), id3v2TagTemplate.getAlbumImage().length, id3v2TagTemplate.getAlbumImageMimeType());
                }
            }
        } else {
            if (albumImageDataTemplate != null) {
                logger.info("AlbumImage to be set: {}, {}", id3v2TagTemplate.getAlbumImage().length, id3v2TagTemplate.getAlbumImageMimeType());
            }
        }

        if (id3v2Tag.getAlbumArtist() != null) {
            logger.info("AlbumArtist to be changed: {} - TO EMPTY", id3v2Tag.getAlbumArtist());
        }

        if (id3v2Tag.getComment() != null) {
            logger.info("Comment to be changed: {} - TO EMPTY", id3v2Tag.getComment());
        }

        if (id3v2Tag.getComposer() != null) {
            logger.info("Composer to be changed: {} - TO EMPTY", id3v2Tag.getComposer());
        }

        if (id3v2Tag.getCopyright() != null) {
            logger.info("Copyright to be changed: {} - TO EMPTY", id3v2Tag.getCopyright());
        }

        if (id3v2Tag.getEncoder() != null) {
            logger.info("Encoder to be changed: {} - TO EMPTY", id3v2Tag.getEncoder());
        }

        if (id3v2Tag.getLyrics() != null) {
            logger.info("Lyrics to be changed: {} - TO EMPTY", id3v2Tag.getLyrics());
        }

        if (id3v2Tag.getOriginalArtist() != null) {
            logger.info("OriginalArtist to be changed: {} - TO EMPTY", id3v2Tag.getOriginalArtist());
        }

        if (id3v2Tag.getPublisher() != null) {
            logger.info("Publisher to be changed: {} - TO EMPTY", id3v2Tag.getPublisher());
        }

        if (id3v2Tag.getUrl() != null) {
            logger.info("Url to be changed: {} - TO EMPTY", id3v2Tag.getUrl());
        }
    }

    private void handleID3v2TagTrack(ID3v2 id3v2TagTemplate, int position) {
        // FIXME calculate the current position against the total items found in the folder

        id3v2TagTemplate.setTrack(String.format("%02d", position));
    }

    private void handleID3v2TagTitle(Mp3File mp3File, ID3v2 id3v2TagTemplate, ID3v2 id3v2Tag) {
        // get original title
        String originalTitle = id3v2Tag == null ? null : id3v2Tag.getTitle();

        // get title from original tag or from file name
        String title = StringUtils.isNotBlank(originalTitle) ? originalTitle : mp3Util.extractTitleFromFileName(mp3File);

        // normalize title
        title = normalizeID3v2TagTitle(title);

        // log changes
        if (!title.equals(originalTitle)) {
            logger.info("Title to be changed: {} - {}", originalTitle, title);
        }

        // set title to template
        id3v2TagTemplate.setTitle(title);
    }

    private String normalizeID3v2TagTitle(String title) {
        String normalizedTitle = WordUtils.capitalize(title);

        // FIXME handle special substitution (ie: "(BONUS TRACK)", "III", etc...)
        normalizedTitle = normalizedTitle.replaceAll("(?i) \\(2008 VERSION\\)", " (2008 VERSION)");
        normalizedTitle = normalizedTitle.replaceAll("(?i) \\(2010 VERSION\\)", " (2010 VERSION)");
        normalizedTitle = normalizedTitle.replaceAll("(?i) \\(ACOUSTIC VERSION\\)", " (ACOUSTIC VERSION)");
        normalizedTitle = normalizedTitle.replaceAll("(?i) \\(BONUS TRACK\\)", " (BONUS TRACK)");
        normalizedTitle = normalizedTitle.replaceAll("(?i) \\(DEMO\\)", " (DEMO)");
        normalizedTitle = normalizedTitle.replaceAll("(?i) \\(INSTRUMENTAL\\)", " (INSTRUMENTAL VERSION)");
        normalizedTitle = normalizedTitle.replaceAll("(?i) \\(INSTRUMENTAL VERSION\\)", " (INSTRUMENTAL VERSION)");

        return normalizedTitle.trim();
    }

    private void handleCustomTag(Mp3File mp3File) {
        logger.debug("handleCustomTag(mp3File={})", mp3File.getFilename());

        if (mp3File.hasCustomTag()) {
            logger.debug("Custom tag: {}", mp3File.getCustomTag());

            mp3File.removeCustomTag();
            logger.info("custom tag removed");
        }
    }

}
