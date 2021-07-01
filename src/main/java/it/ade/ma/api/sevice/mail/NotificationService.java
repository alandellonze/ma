package it.ade.ma.api.sevice.mail;

import it.ade.ma.api.sevice.db.model.dto.AlbumDTO;
import it.ade.ma.api.sevice.db.model.dto.BandDTO;
import it.ade.ma.api.sevice.diff.engine.model.DiffRow;
import it.ade.ma.api.sevice.diff.engine.model.DiffRow.DiffType;
import it.ade.ma.api.sevice.diff.model.DiscographyResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class NotificationService {

    private final String maMetalArchivesUrl;
    private final MailService mailService;

    public NotificationService(
            final @Value("${ma.metal-archives.url}") String maMetalArchivesUrl,
            final MailService mailService) {
        this.maMetalArchivesUrl = maMetalArchivesUrl;
        this.mailService = mailService;
    }

    @Async
    public void execute(DiscographyResult discographyResult) {
        log.info("execute({})", discographyResult);

        BandDTO bandDTO = discographyResult.getBandDTO();
        Integer changes = discographyResult.getChanges();
        List<DiffRow<AlbumDTO>> diffs = discographyResult.getAlbumDiffs();

        String subject = prepareSubject(bandDTO, changes);
        String text = prepareText(bandDTO, diffs);

        mailService.sendEmail(subject, text);
    }

    private String prepareSubject(BandDTO bandDTO, Integer changes) {
        log.debug("prepareSubject({}, {})", bandDTO, changes);

        return bandDTO.getName() + " (" + changes + " differences)";
    }

    private String prepareText(BandDTO bandDTO, List<DiffRow<AlbumDTO>> diffs) {
        log.debug("prepareText({}, {})", bandDTO, diffs);

        StringBuilder document = new StringBuilder();

        document.append("<table cellspacing='0' cellpadding='5' style='font-size: 12px; border-bottom: 1px SOLID #AAAAAA;'>");

        for (DiffRow<AlbumDTO> diff : diffs) {
            switch (diff.getType()) {
                case EQUAL:
                    for (AlbumDTO albumOriginal : diff.getOriginal()) {
                        document.append("<tr>");
                        generateRows(document, diff.getType(), albumOriginal, null);
                        document.append("</tr>");
                    }
                    break;

                case PLUS:
                    for (AlbumDTO albumRevised : diff.getRevised()) {
                        document.append("<tr style='background-color: #77FF77; color: #7777FF;'>");
                        generateRows(document, diff.getType(), null, albumRevised);
                        document.append("</tr>");
                    }
                    break;

                case MINUS:
                    for (AlbumDTO albumOriginal : diff.getOriginal()) {
                        document.append("<tr style='background-color: #FF7777; color: #FFFF77;'>");
                        generateRows(document, diff.getType(), albumOriginal, null);
                        document.append("</tr>");
                    }
                    break;

                case CHANGE:
                    int i = 0;
                    for (; i < diff.getOriginal().size(); i++) {
                        AlbumDTO albumOriginal = diff.getOriginal().get(i);
                        AlbumDTO albumRevised = (i < diff.getRevised().size()) ? diff.getRevised().get(i) : null;

                        document.append("<tr style='background-color: #EEECC0; color: #555555;'>");
                        generateRows(document, diff.getType(), albumOriginal, albumRevised);
                        document.append("<tr>");
                    }

                    if (i < diff.getRevised().size()) {
                        for (; i < diff.getRevised().size(); i++) {
                            AlbumDTO albumRevised = diff.getRevised().get(i);

                            document.append("<tr style='background-color: #EEECC0; color: #555555;'>");
                            generateRows(document, diff.getType(), null, albumRevised);
                            document.append("<tr>");
                        }
                    }
                    break;
            }
        }

        document.append("</table>");

        // add original source
        String url = String.format(maMetalArchivesUrl, bandDTO.getMaKey());
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
