package it.ade.ma.api.sevice;

import it.ade.ma.api.model.dto.AlbumDTO;
import it.ade.ma.api.model.dto.AlbumDiff;
import it.ade.ma.api.model.dto.AlbumDiff.DiffType;
import it.ade.ma.api.model.dto.DiscographyResult;
import it.ade.ma.api.model.entity.Band;
import it.ade.ma.api.util.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final static Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private String maMetalArchivesUrl;
    private MailService mailService;

    public NotificationService(
            @Value("${ma.metal-archives.url}") String maMetalArchivesUrl,
            MailService mailService) {
        this.maMetalArchivesUrl = maMetalArchivesUrl;
        this.mailService = mailService;
    }

    @Async
    void execute(DiscographyResult discographyResult) {
        logger.info("execute({})", discographyResult);

        Band band = discographyResult.getBand();
        Integer changes = discographyResult.getChanges();
        List<AlbumDiff> albumDiffs = discographyResult.getAlbumDiffs();

        String subject = prepareSubject(band, changes);
        String text = prepareText(band, albumDiffs);

        mailService.sendEmail(subject, text);
    }

    private String prepareSubject(Band band, Integer changes) {
        logger.debug("prepareSubject({}, {})", band, changes);

        return band.getName() + " (" + changes + " differences)";
    }

    private String prepareText(Band band, List<AlbumDiff> albumDiffs) {
        logger.debug("prepareText({}, {})", band, albumDiffs);

        StringBuilder document = new StringBuilder();

        document.append("<table cellspacing='0' cellpadding='5' style='font-size: 12px; border-bottom: 1px SOLID #AAAAAA;'>");

        for (AlbumDiff albumDiff : albumDiffs) {
            switch (albumDiff.getType()) {
                case EQUAL:
                    for (AlbumDTO albumOriginal : albumDiff.getOriginal()) {
                        document.append("<tr>");
                        generateRows(document, albumDiff.getType(), albumOriginal, null);
                        document.append("</tr>");
                    }
                    break;

                case PLUS:
                    for (AlbumDTO albumRevised : albumDiff.getRevised()) {
                        document.append("<tr style='background-color: #77FF77; color: #7777FF;'>");
                        generateRows(document, albumDiff.getType(), null, albumRevised);
                        document.append("</tr>");
                    }
                    break;

                case MINUS:
                    for (AlbumDTO albumOriginal : albumDiff.getOriginal()) {
                        document.append("<tr style='background-color: #FF7777; color: #FFFF77;'>");
                        generateRows(document, albumDiff.getType(), albumOriginal, null);
                        document.append("</tr>");
                    }
                    break;

                case CHANGE:
                    int i = 0;
                    for (; i < albumDiff.getOriginal().size(); i++) {
                        AlbumDTO albumOriginal = albumDiff.getOriginal().get(i);
                        AlbumDTO albumRevised = (i < albumDiff.getRevised().size()) ? albumDiff.getRevised().get(i) : null;

                        document.append("<tr style='background-color: #EEECC0; color: #555555;'>");
                        generateRows(document, albumDiff.getType(), albumOriginal, albumRevised);
                        document.append("<tr>");
                    }

                    if (i < albumDiff.getRevised().size()) {
                        for (; i < albumDiff.getRevised().size(); i++) {
                            AlbumDTO albumRevised = albumDiff.getRevised().get(i);

                            document.append("<tr style='background-color: #EEECC0; color: #555555;'>");
                            generateRows(document, albumDiff.getType(), null, albumRevised);
                            document.append("<tr>");
                        }
                    }
                    break;
            }
        }

        document.append("</table>");

        // add original source
        String url = String.format(maMetalArchivesUrl, band.getMaKey());
        document.append("<br />").append("<b>source</b>: ").append(url);

        return document.toString();
    }

    private void generateRows(StringBuilder document, DiffType diffType, AlbumDTO albumOriginal, AlbumDTO albumRevised) {
        generateDiffType(document, diffType);

        if (albumOriginal != null) {
            generatePosition(document, albumOriginal);
            generateType(document, albumOriginal);
            generateTypeCount(document, albumOriginal);
            generateName(document, albumOriginal);
            generateYear(document, albumOriginal);
            generateStatus(document, albumOriginal);
        } else {
            generateTd(document, true);
            generateTd(document, true);
            generateTd(document, true);
            generateTd(document, true);
            generateTd(document, true);
            generateTd(document, true);
        }

        if (albumRevised != null) {
            generateType(document, albumRevised);
            generateTypeCount(document, albumRevised);
            generateName(document, albumRevised);
            generateYear(document, albumRevised);
        } else {
            generateTd(document, true);
            generateTd(document, true);
            generateTd(document, true);
            generateTd(document, true);
        }
    }

    private void generateDiffType(StringBuilder document, DiffType diffType) {
        document.append("<td style='border-top: 1px SOLID #AAAAAA;'>");

        switch (diffType) {
            case PLUS:
                document.append("+");
                break;

            case MINUS:
                document.append("-");
                break;

            case CHANGE:
                document.append(">");
                break;
        }

        document.append("</td>");
    }

    private void generatePosition(StringBuilder document, AlbumDTO album) {
        generateTd(document);
        document.append(album.getPosition())
                .append("</td>");
    }

    private void generateType(StringBuilder document, AlbumDTO album) {
        generateTd(document);
        if (album.getMaType() != null) {
            document.append("<i>").append(album.getMaType()).append("</i>");
        } else if (album.getType() != null) {
            document.append(album.getType());
        }
        document.append("</td>");
    }

    private void generateTypeCount(StringBuilder document, AlbumDTO album) {
        generateTd(document);
        if (album.getMaTypeCount() != null) {
            document.append("<i>").append(String.format("%02d", album.getMaTypeCount())).append("</i>");
        } else if (album.getTypeCount() != null) {
            document.append(String.format("%02d", album.getTypeCount()));
        }
        document.append("</td>");
    }

    private void generateName(StringBuilder document, AlbumDTO album) {
        generateTd(document);
        if (album.getMaName() != null) {
            document.append("<i>").append(album.getMaName()).append("</i>");
        } else if (album.getName() != null) {
            document.append(album.getName());
        }
        document.append("</td>");
    }

    private void generateYear(StringBuilder document, AlbumDTO album) {
        generateTd(document);
        document.append(album.getYear())
                .append("</td>");
    }

    private void generateStatus(StringBuilder document, AlbumDTO album) {
        String status;
        switch (album.getStatus()) {
            case NONE:
                status = "?";
                break;

            case MISSED:
                status = "x";
                break;

            case PRESENT:
            case PRESENT_WITH_COVER:
                status = "v";
                break;

            default:
                status = "";
        }

        generateTd(document);
        document.append(status)
                .append("</td>");
    }

    private void generateTd(StringBuilder document) {
        generateTd(document, false);
    }

    private void generateTd(StringBuilder document, boolean withClosure) {
        document.append("<td style='border-left: 1px SOLID #AAAAAA; border-top: 1px SOLID #AAAAAA;'>");
        if (withClosure) {
            document.append("</td>");
        }
    }

}
