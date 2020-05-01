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

@Service
public class MP3Service {

    private final static Logger logger = LoggerFactory.getLogger(MP3Service.class);

    private PathUtil pathUtil;
    private MP3Util mp3Util;

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
                System.out.println(path + " " + exists);
                if (exists) {
                    status = MP3Status.PRESENT;
                }

                // look into tmp folder
                else {
                    path = pathUtil.generateTMPName(album);
                    exists = pathUtil.fileExists(path);
                    System.out.println(path + " " + exists);
                    status = exists ? MP3Status.TMP : MP3Status.NOT_PRESENT;
                }

                // update status
                album.setMp3Status(status);
            });
        }
    }

    public void adjustAlbumFolder() throws Exception {
        AlbumDTO album = new AlbumDTO();

        // test 1
        // album.setBandName("Blaze Bayley");
        // album.setType("FULLLENGTH");
        // album.setTypeCount(6);
        // album.setName("The Redemption Of William Black (Infinite Entanglement Part III)");
        // album.setYear(2018);

        // test 2
        // album.setBandName("Avantasia");
        // album.setType("FULLLENGTH");
        // album.setTypeCount(8);
        // album.setName("Moonglow");
        // album.setYear(2019);

        // test 3
        // album.setBandName("Trick Or Treat");
        // album.setType("FULLLENGTH");
        // album.setTypeCount(5);
        // album.setName("Re-Animated");
        // album.setYear(2018);

        // test 4
        album.setBandName("Athlantis");
        album.setType("FULLLENGTH");
        album.setTypeCount(5);
        album.setName("The Way To Rock 'n' Roll");
        album.setYear(2019);

        adjustAlbumFolder(album);
    }

    private void adjustAlbumFolder(AlbumDTO album) throws Exception {
        logger.info("adjustAlbumFolder({})", album);

        // create the id3v2 template
        ID3v2 id3v2TagTemplate = mp3Util.createID3v2Template(album);

        // adjust all the mp3 files
        List<String> mp3FileNames = pathUtil.getMP3FileNameList(album);
        mp3FileNames.forEach(mp3FileName -> handleMp3(id3v2TagTemplate, mp3FileName));
    }

    private void handleMp3(ID3v2 id3v2TagTemplate, String mp3FileName) {
        logger.info("handleMp3(id3v2TagTemplate, {})", mp3FileName);

        try {
            Mp3File mp3File = new Mp3File(mp3FileName);

            // id3v1 tag
            boolean changed = handleID3v1Tag(mp3File);

            // id3v2 tag
            changed = handleID3v2Tag(mp3File, id3v2TagTemplate) || changed;

            // custom tag
            changed = handleCustomTag(mp3File) || changed;

            // FIXME normalize file name too

            if (changed) {
                mp3Util.updateMP3File(mp3File);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private boolean handleID3v1Tag(Mp3File mp3File) {
        logger.debug("handleID3v1Tag(mp3File={})", mp3File.getFilename());

        boolean changed = false;

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
            changed = true;
            logger.info("v1 tag removed");
        }

        return changed;
    }

    private boolean handleID3v2Tag(Mp3File mp3File, ID3v2 id3v2TagTemplate) {
        logger.debug("handleID3v2Tag(mp3File={}, id3v2TagTemplate)", mp3File.getFilename());

        boolean changed = false;

        if (mp3File.hasId3v2Tag()) {
            ID3v2 id3v2Tag = mp3File.getId3v2Tag();
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

            // substitute tag's values
            changed = handleID3v2TagFieldsToSubstitute(id3v2Tag, id3v2TagTemplate);

            // handle image
            changed = handleID3v2TagImage(id3v2Tag, id3v2TagTemplate) || changed;

            // handle track
            changed = handleID3v2TagTrack(id3v2Tag) || changed;

            // handle title
            changed = handleID3v2TagTitle(id3v2Tag, mp3File) || changed;

            // empty tag's values
            changed = handleID3v2TagFieldsToBeEmpty(id3v2Tag) || changed;
        }

        // change the id3v2 tag
        if (changed) {
            mp3File.removeId3v2Tag();
            mp3File.setId3v2Tag(id3v2TagTemplate);
        }

        return changed;
    }

    private boolean handleID3v2TagFieldsToSubstitute(ID3v2 id3v2Tag, ID3v2 id3v2TagTemplate) {
        boolean changed = false;

        if (id3v2Tag.getArtist() == null || !id3v2Tag.getArtist().equals(id3v2TagTemplate.getArtist())) {
            logger.info("Artist to be changed: {} - {}", id3v2Tag.getArtist(), id3v2TagTemplate.getArtist());
            // id3v2Tag.setArtist(id3v2TagTemplate.getArtist());
            changed = true;
        }

        if (id3v2Tag.getAlbum() == null || !id3v2Tag.getAlbum().equals(id3v2TagTemplate.getAlbum())) {
            logger.info("Album to be changed: {} - {}", id3v2Tag.getAlbum(), id3v2TagTemplate.getAlbum());
            // id3v2Tag.setAlbum(id3v2TagTemplate.getAlbum());
            changed = true;
        }

        if (id3v2Tag.getYear() == null || !id3v2Tag.getYear().equals(id3v2TagTemplate.getYear())) {
            logger.info("Year to be changed: {} - {}", id3v2Tag.getYear(), id3v2TagTemplate.getYear());
            // id3v2Tag.setYear(id3v2TagTemplate.getYear());
            changed = true;
        }

        if (id3v2Tag.getGenre() != id3v2TagTemplate.getGenre()) {
            logger.info("Genre to be changed: {} - {}", id3v2Tag.getGenre(), id3v2TagTemplate.getGenre());
            // id3v2Tag.setGenre(id3v2TagTemplate.getGenre());
            changed = true;
        }

        if (id3v2Tag.getGenreDescription() == null || !id3v2Tag.getGenreDescription().equals(id3v2TagTemplate.getGenreDescription())) {
            logger.info("GenreDescription to be changed: {} - {}", id3v2Tag.getGenreDescription(), id3v2TagTemplate.getGenreDescription());
            // id3v2Tag.setGenreDescription(id3v2TagTemplate.getGenreDescription());
            changed = true;
        }

        return changed;
    }

    private boolean handleID3v2TagImage(ID3v2 id3v2Tag, ID3v2 id3v2TagTemplate) {
        boolean changed = false;

        byte[] albumImageData = id3v2Tag.getAlbumImage();
        byte[] albumImageDataTemplate = id3v2TagTemplate.getAlbumImage();
        if (albumImageData != null) {
            if (albumImageDataTemplate != null) {
                if (albumImageData.length != albumImageDataTemplate.length || !id3v2Tag.getAlbumImageMimeType().equals(id3v2TagTemplate.getAlbumImageMimeType())) {
                    logger.info("AlbumImage to be changed: {}, {} - {}, {}", id3v2Tag.getAlbumImage().length, id3v2Tag.getAlbumImageMimeType(), id3v2TagTemplate.getAlbumImage().length, id3v2TagTemplate.getAlbumImageMimeType());
                    // id3v2Tag.setAlbumImage(id3v2TagTemplate.getAlbumImage(), id3v2TagTemplate.getAlbumImageMimeType());
                    changed = true;
                }
            }
        } else {
            if (albumImageDataTemplate != null) {
                logger.info("AlbumImage to be set: {}, {}", id3v2TagTemplate.getAlbumImage().length, id3v2TagTemplate.getAlbumImageMimeType());
                // id3v2Tag.setAlbumImage(id3v2TagTemplate.getAlbumImage(), id3v2TagTemplate.getAlbumImageMimeType());
                changed = true;
            }
        }

        return changed;
    }

    private boolean handleID3v2TagTrack(ID3v2 id3v2Tag) {
        boolean changed = false;

        // FIXME calculate the current position against the total item found in the folder

        return changed;
    }

    private boolean handleID3v2TagTitle(ID3v2 id3v2Tag, Mp3File mp3File) {
        boolean changed = false;

        String title;

        // get title from tag or from file name
        String originalTitle = id3v2Tag.getTitle();
        if (StringUtils.isNotEmpty(originalTitle)) {
            title = originalTitle;
        } else {
            title = mp3Util.extractTitleFromFileName(mp3File);
        }

        // normalize
        title = WordUtils.capitalize(title);

        // FIXME handle special substitution (ie: "(BONUS TRACK)", "III", etc...)

        if (!title.equals(originalTitle)) {
            logger.info("Title to be set: {}", title);
            id3v2Tag.setTitle(title);
            changed = true;
        }

        return changed;
    }

    private boolean handleID3v2TagFieldsToBeEmpty(ID3v2 id3v2Tag) {
        boolean changed = false;

        if (id3v2Tag.getAlbumArtist() != null) {
            logger.info("AlbumArtist to be changed: {} - TO EMPTY", id3v2Tag.getAlbumArtist());
            // id3v2Tag.clearFrameSet(ID3v22Tag.ID_ALBUM_ARTIST);
            changed = true;
        }

        if (id3v2Tag.getComment() != null) {
            logger.info("Comment to be changed: {} - TO EMPTY", id3v2Tag.getComment());
            // id3v2Tag.clearFrameSet(ID3v22Tag.ID_COMMENT);
            changed = true;
        }

        if (id3v2Tag.getComposer() != null) {
            logger.info("Composer to be changed: {} - TO EMPTY", id3v2Tag.getComposer());
            // id3v2Tag.clearFrameSet(ID3v22Tag.ID_COMPOSER);
            changed = true;
        }

        if (id3v2Tag.getCopyright() != null) {
            logger.info("Copyright to be changed: {} - TO EMPTY", id3v2Tag.getCopyright());
            // id3v2Tag.clearFrameSet(ID3v22Tag.ID_COPYRIGHT);
            changed = true;
        }

        if (id3v2Tag.getEncoder() != null) {
            logger.info("Encoder to be changed: {} - TO EMPTY", id3v2Tag.getEncoder());
            // id3v2Tag.clearFrameSet(ID3v22Tag.ID_ENCODER);
            changed = true;
        }

        if (id3v2Tag.getLyrics() != null) {
            logger.info("Lyrics to be changed: {} - TO EMPTY", id3v2Tag.getLyrics());
            // id3v2Tag.clearFrameSet(ID3v22Tag.ID_TEXT_LYRICS);
            changed = true;
        }

        if (id3v2Tag.getOriginalArtist() != null) {
            logger.info("OriginalArtist to be changed: {} - TO EMPTY", id3v2Tag.getOriginalArtist());
            // id3v2Tag.clearFrameSet(ID3v22Tag.ID_ORIGINAL_ARTIST);
            changed = true;
        }

        if (id3v2Tag.getPublisher() != null) {
            logger.info("Publisher to be changed: {} - TO EMPTY", id3v2Tag.getPublisher());
            // id3v2Tag.clearFrameSet(ID3v22Tag.ID_PUBLISHER);
            changed = true;
        }

        if (id3v2Tag.getUrl() != null) {
            logger.info("Url to be changed: {} - TO EMPTY", id3v2Tag.getUrl());
            // id3v2Tag.clearFrameSet(ID3v22Tag.ID_URL);
            changed = true;
        }

        return changed;
    }

    private boolean handleCustomTag(Mp3File mp3File) {
        logger.debug("handleCustomTag(mp3File={})", mp3File.getFilename());

        boolean changed = false;

        if (mp3File.hasCustomTag()) {
            logger.debug("Custom tag: {}", mp3File.getCustomTag());

            mp3File.removeCustomTag();
            changed = true;
            logger.info("custom tag removed");
        }

        return changed;
    }

}
