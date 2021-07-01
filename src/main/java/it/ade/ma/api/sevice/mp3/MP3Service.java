package it.ade.ma.api.sevice.mp3;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;
import it.ade.ma.api.constants.MP3Status;
import it.ade.ma.api.sevice.db.model.dto.AlbumDTO;
import it.ade.ma.api.sevice.db.service.AlbumService;
import it.ade.ma.api.sevice.path.PathUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MP3Service {

    private final AlbumService albumService;
    private final PathUtil pathUtil;
    private final MP3Util mp3Util;

    public List<String> getAllMP3s(String bandName) throws IOException {
        return pathUtil.getAllMP3s(bandName)
                .collect(Collectors.toList());
    }

    @Deprecated
    public void findAndUpdate(List<AlbumDTO> albums) {
        log.info("findAndUpdate({})", (albums != null ? albums.size() : null));

        if (albums != null) {
            albums.forEach(album -> {
                MP3Status status;

                // look into mp3 folder
                String path = pathUtil.generateMP3NameFull(album);
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

    public void check(Long albumId) throws RuntimeException {
        log.info("check({})", albumId);

        try {
            // get the Album
            Optional<AlbumDTO> albumOpt = albumService.findById(albumId);
            if (albumOpt.isEmpty()) {
                log.info("album with id: {} wasn't found", albumId);
            } else {
                AlbumDTO albumDTO = albumOpt.get();

                // adjust all the mp3 files
                Map<String, List<String>> mp3FileNameMap = pathUtil.getMP3FileNameMap(albumDTO);
                log.info("found {} cd for '{}'", mp3FileNameMap.size(), albumDTO);
                for (Map.Entry<String, List<String>> entry : mp3FileNameMap.entrySet()) {
                    // create the id3v2 template
                    String cdName = entry.getKey();
                    ID3v2 id3v2TagTemplate = mp3Util.createID3v2Template(cdName, albumDTO);

                    // handle files
                    List<String> mp3FileNames = entry.getValue();
                    log.info("found {} files for '{}'", mp3FileNames.size(), id3v2TagTemplate.getAlbum());
                    for (int i = 0; i < mp3FileNames.size(); i++) {
                        handleMp3(mp3FileNames.get(i), i + 1, id3v2TagTemplate);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void handleMp3(String mp3FileName, int position, ID3v2 id3v2TagTemplate) {
        log.info("handleMp3({}, {}, id3v2TagTemplate)", mp3FileName, position);

        try {
            Mp3File mp3File = new Mp3File(mp3FileName);

            // id3v1 tag
            handleID3v1Tag(mp3File);

            // id3v2 tag
            handleID3v2Tag(mp3File, position, id3v2TagTemplate);

            // custom tag
            handleCustomTag(mp3File);

            // save and normalize file name
            mp3Util.updateMP3File(mp3File);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void handleID3v1Tag(Mp3File mp3File) {
        log.debug("handleID3v1Tag(mp3File={})", mp3File.getFilename());

        if (mp3File.hasId3v1Tag()) {
            ID3v1 id3v1Tag = mp3File.getId3v1Tag();
            log.debug("Artist: {}", id3v1Tag.getArtist());
            log.debug("Album: {}", id3v1Tag.getAlbum());
            log.debug("Year: {}", id3v1Tag.getYear());
            log.debug("Genre: {} ({})", id3v1Tag.getGenre(), id3v1Tag.getGenreDescription());
            log.debug("Track: {}", id3v1Tag.getTrack());
            log.debug("Title: {}", id3v1Tag.getTitle());
            log.debug("Comment: {}", id3v1Tag.getComment());

            mp3File.removeId3v1Tag();
            log.info("v1 tag removed");
        }
    }

    private void handleID3v2Tag(Mp3File mp3File, int position, ID3v2 id3v2TagTemplate) {
        log.debug("handleID3v2Tag(mp3File={}, {}, id3v2TagTemplate)", mp3File.getFilename(), position);

        ID3v2 id3v2Tag = null;
        if (mp3File.hasId3v2Tag()) {
            id3v2Tag = mp3File.getId3v2Tag();
            log.debug("Artist: {}", id3v2Tag.getArtist());
            log.debug("Album: {}", id3v2Tag.getAlbum());
            log.debug("Year: {}", id3v2Tag.getYear());
            log.debug("Genre: {} ({})", id3v2Tag.getGenre(), id3v2Tag.getGenreDescription());
            byte[] albumImageData = id3v2Tag.getAlbumImage();
            if (albumImageData != null) {
                log.debug("Album image, length: {} bytes, mime type: {}", albumImageData.length, id3v2Tag.getAlbumImageMimeType());
            }
            log.debug("Track: {}", id3v2Tag.getTrack());
            log.debug("Title: {}", id3v2Tag.getTitle());
            log.debug("Album artist: {}", id3v2Tag.getAlbumArtist());
            log.debug("Comment: {}", id3v2Tag.getComment());
            log.debug("Composer: {}", id3v2Tag.getComposer());
            log.debug("Copyright: {}", id3v2Tag.getCopyright());
            log.debug("Encoder: {}", id3v2Tag.getEncoder());
            log.debug("Lyrics: {}", id3v2Tag.getLyrics());
            log.debug("Original artist: {}", id3v2Tag.getOriginalArtist());
            log.debug("Publisher: {}", id3v2Tag.getPublisher());
            log.debug("URL: {}", id3v2Tag.getUrl());

            // log changes to be applied
            logID3v2TagFieldsToBeChanged(id3v2Tag, id3v2TagTemplate);
        }

        // handle track
        handleID3v2TagTrack(position, id3v2TagTemplate, id3v2Tag);

        // handle title
        handleID3v2TagTitle(mp3File, id3v2TagTemplate, id3v2Tag);

        // change the id3v2 tag
        mp3File.removeId3v2Tag();
        mp3File.setId3v2Tag(id3v2TagTemplate);
    }

    private void logID3v2TagFieldsToBeChanged(ID3v2 id3v2Tag, ID3v2 id3v2TagTemplate) {
        if (id3v2Tag.getArtist() == null || !id3v2Tag.getArtist().equals(id3v2TagTemplate.getArtist())) {
            log.info("Artist to be changed: {} => {}", id3v2Tag.getArtist(), id3v2TagTemplate.getArtist());
        }

        if (id3v2Tag.getAlbum() == null || !id3v2Tag.getAlbum().equals(id3v2TagTemplate.getAlbum())) {
            log.info("Album to be changed: {} => {}", id3v2Tag.getAlbum(), id3v2TagTemplate.getAlbum());
        }

        if (id3v2Tag.getYear() == null || !id3v2Tag.getYear().equals(id3v2TagTemplate.getYear())) {
            log.info("Year to be changed: {} => {}", id3v2Tag.getYear(), id3v2TagTemplate.getYear());
        }

        if (id3v2Tag.getGenre() != id3v2TagTemplate.getGenre()) {
            log.info("Genre to be changed: {} => {}", id3v2Tag.getGenre(), id3v2TagTemplate.getGenre());
        }

        if (id3v2Tag.getGenreDescription() == null || !id3v2Tag.getGenreDescription().equals(id3v2TagTemplate.getGenreDescription())) {
            log.info("GenreDescription to be changed: {} => {}", id3v2Tag.getGenreDescription(), id3v2TagTemplate.getGenreDescription());
        }

        byte[] albumImageData = id3v2Tag.getAlbumImage();
        byte[] albumImageDataTemplate = id3v2TagTemplate.getAlbumImage();
        if (albumImageData != null) {
            if (albumImageDataTemplate != null) {
                if (albumImageData.length != albumImageDataTemplate.length || !id3v2Tag.getAlbumImageMimeType().equals(id3v2TagTemplate.getAlbumImageMimeType())) {
                    log.info("AlbumImage to be changed: {}, {} => {}, {}", id3v2Tag.getAlbumImage().length, id3v2Tag.getAlbumImageMimeType(), id3v2TagTemplate.getAlbumImage().length, id3v2TagTemplate.getAlbumImageMimeType());
                }
            }
        } else {
            if (albumImageDataTemplate != null) {
                log.info("AlbumImage to be set: {}, {}", id3v2TagTemplate.getAlbumImage().length, id3v2TagTemplate.getAlbumImageMimeType());
            }
        }

        if (id3v2Tag.getAlbumArtist() != null) {
            log.info("AlbumArtist to be changed: {} => TO EMPTY", id3v2Tag.getAlbumArtist());
        }

        if (id3v2Tag.getComment() != null) {
            log.info("Comment to be changed: {} => TO EMPTY", id3v2Tag.getComment());
        }

        if (id3v2Tag.getComposer() != null) {
            log.info("Composer to be changed: {} => TO EMPTY", id3v2Tag.getComposer());
        }

        if (id3v2Tag.getCopyright() != null) {
            log.info("Copyright to be changed: {} => TO EMPTY", id3v2Tag.getCopyright());
        }

        if (id3v2Tag.getEncoder() != null) {
            log.info("Encoder to be changed: {} => TO EMPTY", id3v2Tag.getEncoder());
        }

        if (id3v2Tag.getLyrics() != null) {
            log.info("Lyrics to be changed: {} => TO EMPTY", id3v2Tag.getLyrics());
        }

        if (id3v2Tag.getOriginalArtist() != null) {
            log.info("OriginalArtist to be changed: {} => TO EMPTY", id3v2Tag.getOriginalArtist());
        }

        if (id3v2Tag.getPublisher() != null) {
            log.info("Publisher to be changed: {} => TO EMPTY", id3v2Tag.getPublisher());
        }

        if (id3v2Tag.getUrl() != null) {
            log.info("Url to be changed: {} => TO EMPTY", id3v2Tag.getUrl());
        }
    }

    private void handleID3v2TagTrack(int position, ID3v2 id3v2TagTemplate, ID3v2 id3v2Tag) {
        // get original track
        String originalTrack = id3v2Tag == null ? null : id3v2Tag.getTrack();

        // prepare track
        String track = String.format("%02d", position);

        // log changes
        if (!track.equals(originalTrack)) {
            log.info("Track to be changed: {} => {}", originalTrack, track);
        }

        // set track to template
        id3v2TagTemplate.setTrack(track);
    }

    private void handleID3v2TagTitle(Mp3File mp3File, ID3v2 id3v2TagTemplate, ID3v2 id3v2Tag) {
        // get original title
        String originalTitle = id3v2Tag == null ? null : id3v2Tag.getTitle();

        // get title from original tag or from file name
        String title = StringUtils.isNotBlank(originalTitle) ? originalTitle : mp3Util.extractTitleFromMp3File(mp3File);

        // normalize title
        title = normalizeID3v2TagTitle(title);

        // log changes
        if (!title.equals(originalTitle)) {
            log.info("Title to be changed: {} => {}", originalTitle, title);
        }

        // set title to template
        id3v2TagTemplate.setTitle(title);
    }

    private String normalizeID3v2TagTitle(String title) {
        String normalizedTitle = WordUtils.capitalize(title);

        normalizedTitle = normalizedTitle.replaceAll("´", "'");

        // FIXME handle special substitution (ie: "(BONUS TRACK)", "III", etc...)
        normalizedTitle = normalizedTitle.replaceAll("(?i) \\(2008 VERSION\\)", " (2008 VERSION)");
        normalizedTitle = normalizedTitle.replaceAll("(?i) \\(2010 VERSION\\)", " (2010 VERSION)");

        normalizedTitle = normalizedTitle.replaceAll("(?i) \\(ACOUSTIC VERSION\\)", " (ACOUSTIC VERSION)");
        normalizedTitle = normalizedTitle.replaceAll("(?i) \\(ACOUSTIC\\)", " (ACOUSTIC VERSION)");

        normalizedTitle = normalizedTitle.replaceAll("(?i) \\(ALTERNATIVE MIX\\)", " (ALTERNATIVE MIX)");

        normalizedTitle = normalizedTitle.replaceAll("(?i) \\(REMIX\\)", " (REMIX)");

        normalizedTitle = normalizedTitle.replaceAll("(?i) \\(APOCALYPSE VERSION\\)", " (APOCALYPSE VERSION)");
        normalizedTitle = normalizedTitle.replaceAll("(?i) \\(APOCALYPSE\\)", " (APOCALYPSE VERSION)");

        normalizedTitle = normalizedTitle.replaceAll("(?i) \\(BONUS TRACK\\)", " (BONUS TRACK)");
        normalizedTitle = normalizedTitle.replaceAll("(?i) \\(BONUS\\)", " (BONUS TRACK)");

        normalizedTitle = normalizedTitle.replaceAll("(?i) COVER\\)", " COVER)");

        normalizedTitle = normalizedTitle.replaceAll("(?i) \\(REMASTERED\\)", " (REMASTERED )");

        normalizedTitle = normalizedTitle.replaceAll("(?i) \\(RADIO EDIT\\)", " (RADIO EDIT)");
        normalizedTitle = normalizedTitle.replaceAll("(?i) \\(RADIO\\)", " (RADIO EDIT)");

        normalizedTitle = normalizedTitle.replaceAll("(?i) \\(DEMO VERSION\\)", " (DEMO)");
        normalizedTitle = normalizedTitle.replaceAll("(?i) \\(DEMO\\)", " (DEMO)");

        normalizedTitle = normalizedTitle.replaceAll("(?i) \\(EDIT VERSION\\)", " (EDIT VERSION)");
        normalizedTitle = normalizedTitle.replaceAll("(?i) \\(SINGLE EDIT\\)", " (EDIT VERSION)");
        normalizedTitle = normalizedTitle.replaceAll("(?i) \\(EDIT\\)", " (EDIT VERSION)");

        normalizedTitle = normalizedTitle.replaceAll("(?i) \\(HISTORY VERSION\\)", " (HISTORY VERSION)");
        normalizedTitle = normalizedTitle.replaceAll("(?i) \\(HISTORY\\)", " (HISTORY VERSION)");

        normalizedTitle = normalizedTitle.replaceAll("(?i) \\(INSTRUMENTAL VERSION\\)", " (INSTRUMENTAL VERSION)");
        normalizedTitle = normalizedTitle.replaceAll("(?i) \\(INSTRUMENTAL\\)", " (INSTRUMENTAL VERSION)");

        normalizedTitle = normalizedTitle.replaceAll("(?i) \\(JAP BONUS TRACK\\)", " (JAP BONUS TRACK)");
        normalizedTitle = normalizedTitle.replaceAll("(?i) \\(JAPAN BONUS TRACK\\)", " (JAP BONUS TRACK)");

        normalizedTitle = normalizedTitle.replaceAll("(?i) \\(LIVE\\)", " (LIVE)");
        normalizedTitle = normalizedTitle.replaceAll("(?i) \\(LIVE 2012\\)", " (LIVE)");

        normalizedTitle = normalizedTitle.replaceAll("(?i) \\(LIVE ACOUSTIC REHEARSAL VERSION\\)", " (LIVE ACOUSTIC REHEARSAL VERSION)");

        normalizedTitle = normalizedTitle.replaceAll("(?i) \\(ORCHESTRAL VERSION\\)", " (ORCHESTRAL VERSION)");
        normalizedTitle = normalizedTitle.replaceAll("(?i) \\(ORCHESTRAL\\)", " (ORCHESTRAL VERSION)");

        normalizedTitle = normalizedTitle.replaceAll("(?i) \\(PIANO AND VOCAL VERSION\\)", " (PIANO AND VOCAL VERSION)");

        normalizedTitle = normalizedTitle.replaceAll("(?i) \\(RE-RECORDED VERSION\\)", " (RE-RECORDED VERSION)");
        normalizedTitle = normalizedTitle.replaceAll("(?i) \\(RE-RECORDED\\)", " (RE-RECORDED VERSION)");

        normalizedTitle = normalizedTitle.replaceAll("(?i) \\(SOUNDTRACK VERSION\\)", " (SOUNDTRACK VERSION)");
        normalizedTitle = normalizedTitle.replaceAll("(?i) \\(SOUNDTRACK\\)", " (SOUNDTRACK VERSION)");

        normalizedTitle = normalizedTitle.replaceAll("(?i) \\(STUDIO JAM VERSION\\)", " (STUDIO JAM VERSION)");
        normalizedTitle = normalizedTitle.replaceAll("(?i) \\(STUDIO JAM\\)", " (STUDIO JAM VERSION)");

        normalizedTitle = normalizedTitle.replaceAll("(?i) \\(16TH CENTURY VERSION\\)", " (16TH CENTURY VERSION)");
        normalizedTitle = normalizedTitle.replaceAll("(?i) \\(16TH CENTURY\\)", " (16TH CENTURY VERSION)");

        normalizedTitle = normalizedTitle.replaceAll("(?i) \\(ALTERNATIVE VOCALS VERSION\\)", " (ALTERNATIVE VOCALS VERSION)");
        normalizedTitle = normalizedTitle.replaceAll("(?i) \\(ALTERNATIVE VOCALS\\)", " (ALTERNATIVE VOCALS VERSION)");
        normalizedTitle = normalizedTitle.replaceAll("(?i) \\(ALTERNATIVE VOCAL VERSION\\)", " (ALTERNATIVE VOCALS VERSION)");
        normalizedTitle = normalizedTitle.replaceAll("(?i) \\(ALTERNATIVE VOCAL\\)", " (ALTERNATIVE VOCALS VERSION)");

        normalizedTitle = normalizedTitle.replaceAll("(?i) \\(FULL MARCO VOCALS VERSION\\)", " (FULL MARCO VOCALS VERSION)");

        return normalizedTitle.trim();
    }

    private void handleCustomTag(Mp3File mp3File) {
        log.debug("handleCustomTag(mp3File={})", mp3File.getFilename());

        if (mp3File.hasCustomTag()) {
            log.debug("Custom tag: {}", mp3File.getCustomTag());

            mp3File.removeCustomTag();
            log.info("custom tag removed");
        }
    }

}
