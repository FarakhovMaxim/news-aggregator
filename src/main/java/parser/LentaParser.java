package parser;

import http.HttpFetcher;
import model.Article;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LentaParser implements Parser {
    private final HttpFetcher fetcher;

    public LentaParser(HttpFetcher fetcher) {
        this.fetcher = fetcher;
    }

    @Override
    public List<Article> parse() throws Exception {
        String xml = fetcher.get("https://lenta.ru/rss/news");
        Document doc = Jsoup.parse(xml, "", org.jsoup.parser.Parser.xmlParser());
        Elements items = doc.select("item");
        List<Article> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (Element item : items) {

            String title = item.select("title").text();
            String link = item.select("link").text();
            if (seen.contains(link)) continue;
            seen.add(link);
            String description = item.select("description").text();
            if (description.isEmpty()) {
                description = "отсутсвует";
            }
            String date = item.select("pubDate").text();
            String category = item.select("category").text();

            String fullText = description;

            List<String> images = new ArrayList<>();
            Element enclosure = item.selectFirst("enclosure");
            if (enclosure != null) {
                String url = enclosure.attr("url");
                if (!url.isEmpty()) {
                    images.add(url);
                }
            }

            result.add(new Article(
                    title,
                    description,
                    fullText,
                    link,
                    date,
                    category,
                    "LENTA",
                    images
            ));
        }
        return result;
    }
}
