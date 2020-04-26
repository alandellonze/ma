package it.ade.ma.api.util;

import it.ade.ma.api.model.dto.WebPageAlbum;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class WebPageContentService {

    private final static Logger logger = LoggerFactory.getLogger(WebPageContentService.class);

    @Value("${ma.metal-archives.url}")
    private String maMetalArchivesUrl;

    List<WebPageAlbum> parse(Long bandMAKey) throws IOException {
        logger.info("parse({})", bandMAKey);

        List<WebPageAlbum> webPageAlbums = new ArrayList<>();

        // retrieve the web page content
        String url = String.format(maMetalArchivesUrl, bandMAKey);
        logger.debug("url: {}", url);
        Document doc = Jsoup.connect(url).get();

        // extract all the td's html content
        Elements tds = doc.select("td");
        List<String> tdsHtmlContent = tds.stream().map(td -> td.html()).collect(Collectors.toList());

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
