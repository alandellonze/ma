package it.ade.ma.mp3;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v24Tag;
import com.mpatric.mp3agic.Mp3File;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Deprecated
public class MP3Test {

    public static void main(String[] args) throws Exception {
        // this data should be taken from properties
        String rootFolder = "/Users/ade/Downloads/xxx/";
        String mp3Folder = "mp3/";
        String coversFolder = "covers/";

        // this data should be taken from db
        //   String bandName = "Blaze Bayley";
        //   String albumPosition = "06";
        //   String albumName = "The Redemption Of William Black (Infinite Entanglement Part III)";
        //   String albumYear = "2018";

        String bandName = "Trick Or Treat";
        String albumPosition = "05";
        String albumName = "Re-Animated";
        String albumYear = "2018";

        // this data should be taken from disk
        String albumCover = rootFolder.concat(coversFolder).concat(bandName).concat("/" + albumPosition).concat(" - " + albumName).concat(".jpg");
        byte[] albumCoverImage = Files.readAllBytes(Paths.get(albumCover));
        String albumCoverMime = "image/jpeg";

        // create the  id3v2 template
        ID3v2 id3v2TagTemplate = new ID3v24Tag();
        id3v2TagTemplate.setArtist(bandName);
        id3v2TagTemplate.setAlbum(albumName);
        id3v2TagTemplate.setYear(albumYear);
        id3v2TagTemplate.setGenre(9);
        id3v2TagTemplate.setGenreDescription("Metal");
        id3v2TagTemplate.setAlbumImage(albumCoverImage, albumCoverMime);

        // adjust all the mp3 files
        String albumFolder = rootFolder.concat(mp3Folder).concat(bandName).concat("/" + albumPosition).concat(" - " + albumName);
        List<String> mp3FileNames = listFilesUsingDirectoryStream(albumFolder);
        mp3FileNames.forEach(mp3FileName -> handleMp3(id3v2TagTemplate, mp3FileName));
    }

    private static List<String> listFilesUsingDirectoryStream(String dir) throws IOException {
        List<String> fileList = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(dir))) {
            for (Path path : stream) {
                if (!Files.isDirectory(path)) {
                    fileList.add(dir + "/" + path.getFileName().toString());
                }
            }
        }

        Collections.sort(fileList);
        return fileList;
    }

    private static void handleMp3(ID3v2 id3v2TagTemplate, String mp3FileName) {
        System.out.println("\n" + mp3FileName);

        try {
            boolean changed = false;
            Mp3File mp3file = new Mp3File(mp3FileName);

            // id3v1 tag
            if (mp3file.hasId3v1Tag()) {
                ID3v1 id3v1Tag = mp3file.getId3v1Tag();
                if (false) {
                    System.out.println("ID3v1 - Artist: " + id3v1Tag.getArtist());
                    System.out.println("ID3v1 - Album: " + id3v1Tag.getAlbum());
                    System.out.println("ID3v1 - Year: " + id3v1Tag.getYear());
                    System.out.println("ID3v1 - Genre: " + id3v1Tag.getGenre() + " (" + id3v1Tag.getGenreDescription() + ")");
                    System.out.println("ID3v1 - Track: " + id3v1Tag.getTrack());
                    System.out.println("ID3v1 - Title: " + id3v1Tag.getTitle());
                    System.out.println("ID3v1 - Comment: " + id3v1Tag.getComment());
                }

                System.out.println("ID3v1 - TO BE REMOVED");
                mp3file.removeId3v1Tag();
                changed = true;
            }


            // id3v2 tag
            if (mp3file.hasId3v2Tag()) {
                ID3v2 id3v2Tag = mp3file.getId3v2Tag();
                if (false) {
                    System.out.println("ID3v2 - Artist: " + id3v2Tag.getArtist());
                    System.out.println("ID3v2 - Album: " + id3v2Tag.getAlbum());
                    System.out.println("ID3v2 - Year: " + id3v2Tag.getYear());
                    System.out.println("ID3v2 - Genre: " + id3v2Tag.getGenre() + " (" + id3v2Tag.getGenreDescription() + ")");
                    byte[] albumImageData = id3v2Tag.getAlbumImage();
                    if (albumImageData != null) {
                        System.out.println("ID3v2 - Album image, length: " + albumImageData.length + " bytes, mime type: " + id3v2Tag.getAlbumImageMimeType());
                    }
                    System.out.println("ID3v2 - Track: " + id3v2Tag.getTrack());
                    System.out.println("ID3v2 - Title: " + id3v2Tag.getTitle());

                    System.out.println("ID3v2 - Album artist: " + id3v2Tag.getAlbumArtist());
                    System.out.println("ID3v2 - Comment: " + id3v2Tag.getComment());
                    System.out.println("ID3v2 - Composer: " + id3v2Tag.getComposer());
                    System.out.println("ID3v2 - Copyright: " + id3v2Tag.getCopyright());
                    System.out.println("ID3v2 - Encoder: " + id3v2Tag.getEncoder());
                    System.out.println("ID3v2 - Lyrics: " + id3v2Tag.getLyrics());
                    System.out.println("ID3v2 - Original artist: " + id3v2Tag.getOriginalArtist());
                    System.out.println("ID3v2 - Publisher: " + id3v2Tag.getPublisher());
                    System.out.println("ID3v2 - URL: " + id3v2Tag.getUrl());
                }

                // substitute tag's values
                if (id3v2Tag.getArtist() == null || !id3v2Tag.getArtist().equals(id3v2TagTemplate.getArtist())) {
                    System.out.println("ID3v2 - Artist to be changed: " + id3v2Tag.getArtist() + " - " + id3v2TagTemplate.getArtist());
                    id3v2Tag.setArtist(id3v2TagTemplate.getArtist());
                    changed = true;
                }
                if (id3v2Tag.getAlbum() == null || !id3v2Tag.getAlbum().equals(id3v2TagTemplate.getAlbum())) {
                    System.out.println("ID3v2 - Album to be changed: " + id3v2Tag.getAlbum() + " - " + id3v2TagTemplate.getAlbum());
                    id3v2Tag.setAlbum(id3v2TagTemplate.getAlbum());
                    changed = true;
                }
                if (id3v2Tag.getYear() == null || !id3v2Tag.getYear().equals(id3v2TagTemplate.getYear())) {
                    System.out.println("ID3v2 - Year to be changed: " + id3v2Tag.getYear() + " - " + id3v2TagTemplate.getYear());
                    id3v2Tag.setYear(id3v2TagTemplate.getYear());
                    changed = true;
                }
                if (id3v2Tag.getGenre() != id3v2TagTemplate.getGenre()) {
                    System.out.println("ID3v2 - Genre to be changed: " + id3v2Tag.getGenre() + " - " + id3v2TagTemplate.getGenre());
                    id3v2Tag.setGenre(id3v2TagTemplate.getGenre());
                    changed = true;
                }
                if (id3v2Tag.getGenreDescription() == null || !id3v2Tag.getGenreDescription().equals(id3v2TagTemplate.getGenreDescription())) {
                    System.out.println("ID3v2 - GenreDescription to be changed: " + id3v2Tag.getGenreDescription() + " - " + id3v2TagTemplate.getGenreDescription());
                    id3v2Tag.setGenreDescription(id3v2TagTemplate.getGenreDescription());
                    changed = true;
                }

                byte[] albumImageData = id3v2Tag.getAlbumImage();
                byte[] albumImageDataTemplate = id3v2TagTemplate.getAlbumImage();
                if (albumImageData != null) {
                    if (albumImageDataTemplate != null) {
                        if (albumImageData.length != albumImageDataTemplate.length || !id3v2Tag.getAlbumImageMimeType().equals(id3v2TagTemplate.getAlbumImageMimeType())) {
                            System.out.println("ID3v2 - AlbumImage to be changed: " + (id3v2Tag.getAlbumImage().length + ", " + id3v2Tag.getAlbumImageMimeType()) + " - " + (id3v2TagTemplate.getAlbumImage().length + ", " + id3v2TagTemplate.getAlbumImageMimeType()));
                            id3v2Tag.setAlbumImage(id3v2TagTemplate.getAlbumImage(), id3v2TagTemplate.getAlbumImageMimeType());
                            changed = true;
                        }
                    }
                } else {
                    if (albumImageDataTemplate != null) {
                        System.out.println("ID3v2 - AlbumImage to be set: " + id3v2TagTemplate.getAlbumImage().length + ", " + id3v2TagTemplate.getAlbumImageMimeType());
                        id3v2Tag.setAlbumImage(id3v2TagTemplate.getAlbumImage(), id3v2TagTemplate.getAlbumImageMimeType());
                        changed = true;
                    }
                }

                // FIXME handle track
                // if (id3v2Tag.getTrack() == null || !id3v2Tag.getTrack().equals(id3v2TagTemplate.getTrack())) {
                //    System.out.println("ID3v2 - Track to be changed: " + id3v2Tag.getTrack() + " - " + id3v2TagTemplate.getTrack());
                //    id3v2Tag.setTrack(id3v2TagTemplate.getTrack());
                //    changed = true;
                // }
                if (id3v2TagTemplate.getTrack() == null) {
                    id3v2TagTemplate.setTrack(id3v2Tag.getTrack());
                    changed = true;
                }

                // FIXME handle title
                // if (id3v2Tag.getTitle() == null || !id3v2Tag.getTitle().equals(id3v2TagTemplate.getTitle())) {
                //     System.out.println("ID3v2 - Title to be changed: " + id3v2Tag.getTitle() + " - " + id3v2TagTemplate.getTitle());
                //     id3v2Tag.setTitle(id3v2TagTemplate.getTitle());
                //     changed = true;
                // }
                if (id3v2TagTemplate.getTitle() == null) {
                    id3v2TagTemplate.setTitle(id3v2Tag.getTitle());
                    changed = true;
                }

                if (id3v2Tag.getAlbumArtist() != null) {
                    System.out.println("ID3v2 - AlbumArtist to be changed: " + id3v2Tag.getAlbumArtist() + " - TO BE EMPTY");
                    id3v2Tag.setAlbumArtist(null);
                    changed = true;
                }
                if (id3v2Tag.getComment() != null) {
                    System.out.println("ID3v2 - Comment to be changed: " + id3v2Tag.getComment() + " - TO BE EMPTY");
                    id3v2Tag.setComment(null);
                    changed = true;
                }
                if (id3v2Tag.getComposer() != null) {
                    System.out.println("ID3v2 - Composer to be changed: " + id3v2Tag.getComposer() + " - TO BE EMPTY");
                    id3v2Tag.setComposer(null);
                    changed = true;
                }
                if (id3v2Tag.getCopyright() != null) {
                    System.out.println("ID3v2 - Copyright to be changed: " + id3v2Tag.getCopyright() + " - TO BE EMPTY");
                    id3v2Tag.setCopyright(null);
                    changed = true;
                }
                if (id3v2Tag.getEncoder() != null) {
                    System.out.println("ID3v2 - Encoder to be changed: " + id3v2Tag.getEncoder() + " - TO BE EMPTY");
                    id3v2Tag.setEncoder(null);
                    changed = true;
                }
                if (id3v2Tag.getLyrics() != null) {
                    System.out.println("ID3v2 - Lyrics to be changed: " + id3v2Tag.getLyrics() + " - TO BE EMPTY");
                    id3v2Tag.setLyrics(null);
                    changed = true;
                }
                if (id3v2Tag.getOriginalArtist() != null) {
                    System.out.println("ID3v2 - OriginalArtist to be changed: " + id3v2Tag.getOriginalArtist() + " - TO BE EMPTY");
                    id3v2Tag.setOriginalArtist(null);
                    changed = true;
                }
                if (id3v2Tag.getPublisher() != null) {
                    System.out.println("ID3v2 - Publisher to be changed: " + id3v2Tag.getPublisher() + " - TO BE EMPTY");
                    id3v2Tag.setPublisher(null);
                    changed = true;
                }
                if (id3v2Tag.getUrl() != null) {
                    System.out.println("ID3v2 - Url to be changed: " + id3v2Tag.getUrl() + " - TO BE EMPTY");
                    id3v2Tag.setUrl(null);
                    changed = true;
                }
            }

            // custom tag
            if (mp3file.hasCustomTag()) {
                if (false) {
                    System.out.println("Custom tag: " + mp3file.getCustomTag());
                }

                System.out.println("Custom tag - TO BE REMOVED");
                mp3file.removeCustomTag();
                changed = true;
            }

            if (changed) {
                System.out.println("update: " + mp3FileName);
                mp3file.save(mp3FileName + "_TMP");
                Files.move(Paths.get(mp3FileName + "_TMP"), Paths.get(mp3FileName), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
