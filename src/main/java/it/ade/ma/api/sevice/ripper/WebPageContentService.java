package it.ade.ma.api.sevice.ripper;

import it.ade.ma.api.sevice.ripper.model.WebPageAlbum;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class WebPageContentService {

    private final String maMetalArchivesUrl;

    public WebPageContentService(
            @Value("${ma.metal-archives.url}") String maMetalArchivesUrl) {
        this.maMetalArchivesUrl = maMetalArchivesUrl;
    }

    List<WebPageAlbum> parse(Long bandMAKey) throws IOException {
        log.info("parse({})", bandMAKey);

        List<WebPageAlbum> webPageAlbums = new ArrayList<>();

        // retrieve the web page content
        String url = String.format(maMetalArchivesUrl, bandMAKey);
        log.debug("url: {}", url);
        Document doc = Jsoup.connect(url).get();

        // extract all the td's html content
        Elements tds = doc.select("td");
        List<String> tdsHtmlContent = tds.stream().map(Element::html).collect(Collectors.toList());

        // group tds 4 by 4
        for (int i = 0; i < tdsHtmlContent.size(); i = i + 4) {
            String type = tdsHtmlContent.get(i + 1);
            String name = normalizeHtml(Jsoup.parse(tdsHtmlContent.get(i)).select("a").html());
            String year = tdsHtmlContent.get(i + 2);
            webPageAlbums.add(new WebPageAlbum(type, name, year));
        }

        return webPageAlbums;
    }

    private String normalizeHtml(String html) {
        return html.replaceAll("&amp;", "&");
    }

}
