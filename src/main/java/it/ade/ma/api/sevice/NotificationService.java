package it.ade.ma.api.sevice;

import it.ade.ma.api.model.Album;
import it.ade.ma.api.model.Band;
import it.ade.ma.api.model.dto.AlbumDiff;
import it.ade.ma.api.model.dto.DiscographyResult;
import it.ade.ma.api.util.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private MailService mailService;

    @Async
    public void execute(DiscographyResult discographyResult) {
        Band band = discographyResult.getBand();
        List<AlbumDiff> albumDiffs = discographyResult.getAlbumDiffs();

        String subject = prepareSubject(band, albumDiffs);
        String text = prepareText(band, albumDiffs);

        mailService.sendEmail(subject, text);
    }

    private String prepareSubject(Band band, List<AlbumDiff> albumDiffs) {
        StringBuilder subject = new StringBuilder(band.getName())
                .append(" (").append(albumDiffs.size()).append(" differences)");
        return subject.toString();
    }

    private String prepareText(Band band, List<AlbumDiff> albumDiffs) {
        StringBuilder document = new StringBuilder("<table cellspacing='0' cellpadding='3' style='font-size: 12px; border: 1px SOLID #AAAAAA;'>");

        document.append("<tr style='font-weight: bold;'>")
                .append("<td>")
                .append("</td>")
                .append("<td>")
                .append(band.getName())
                .append("</td>")
                .append("<td>")
                .append("changes")
                .append("</td>")
                .append("</tr>");

        for (AlbumDiff albumDiff : albumDiffs) {
            switch (albumDiff.getType()) {
                case EQUAL:
                    for (Album album : albumDiff.getOriginal()) {
                        document.append("<tr>")
                                .append("<td>")
                                .append(generatePosition(album))
                                .append("</td>")
                                .append("<td>")
                                .append(generateName(album))
                                .append("</td>")
                                .append("<td>")
                                .append("</td>")
                                .append("</tr>");
                    }
                    break;

                case PLUS:
                    for (Album album : albumDiff.getRevised()) {
                        document.append("<tr style='background-color: lightgreen; color: white;'>")
                                .append("<td>")
                                .append("</td>")
                                .append("<td>")
                                .append("</td>")
                                .append("<td>")
                                .append(generateName(album))
                                .append("</td>")
                                .append("</tr>");
                    }
                    break;

                case MINUS:
                    for (Album album : albumDiff.getOriginal()) {
                        document.append("<tr style='background-color: lightcoral; color: white;'>")
                                .append("<td>")
                                .append(generatePosition(album))
                                .append("</td>")
                                .append("<td>")
                                .append(generateName(album))
                                .append("</td>")
                                .append("<td>")
                                .append("</td>")
                                .append("</tr>");
                    }
                    break;

                case CHANGE:
                    document.append("<tr style='background-color: lightgoldenrodyellow; color: brown;'>");

                    int i = 0;
                    for (; i < albumDiff.getOriginal().size(); i++) {
                        Album albumOriginal = albumDiff.getOriginal().get(i);
                        Album albumRevised = (i < albumDiff.getRevised().size()) ? albumDiff.getRevised().get(i) : null;

                        document.append("<td>")
                                .append(generatePosition(albumOriginal))
                                .append("</td>")
                                .append("<td>")
                                .append(generateName(albumOriginal))
                                .append("</td>")
                                .append("<td>");
                        if (albumRevised != null) {
                            document.append(generateName(albumRevised));
                        }
                        document.append("</td>");
                    }

                    if (albumDiff.getRevised().size() < i) {
                        for (; i < albumDiff.getRevised().size(); i++) {
                            Album albumRevised = albumDiff.getRevised().get(i);

                            document.append("<td>")
                                    .append("</td>")
                                    .append("<td>")
                                    .append("</td>")
                                    .append("<td>")
                                    .append(generateName(albumRevised))
                                    .append("</td>");
                        }
                    }

                    document.append("</tr>");
                    break;
            }
        }

        document.append("</table>");

        return document.toString();
    }

    private StringBuilder generatePosition(Album album) {
        return new StringBuilder(String.format("%03d", album.getPosition()));
    }

    private StringBuilder generateName(Album album) {
        return new StringBuilder(album.getType()).append(String.format("%02d", album.getTypeCount())).append(" - ").append(album.getName()).append(" (").append(album.getYear()).append(")");
    }

}
