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

public class RbcParser implements Parser {
    private final HttpFetcher fetcher;

    public RbcParser(HttpFetcher fetcher) {
        this.fetcher = fetcher;
    }

    @Override
    public List<Article> parse() throws Exception {
        String xml = fetcher.get("https://rssexport.rbc.ru/rbcnews/news/30/full.rss");
        Document doc = Jsoup.parse(xml, "", org.jsoup.parser.Parser.xmlParser());
        Elements items = doc.select("item");
        List<Article> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (Element item : items) {
            String title = item.select("title").text();

            String shortDesc = item.select("rbc_news|anons").text().isEmpty()
                            ? item.select("description").text()
                            : item.select("rbc_news|anons").text();

            String fullText = item.select("rbc_news|full-text").text();
            String link = item.select("link").text();
            if (seen.contains(link)) continue;

            seen.add(link);
            String date = item.select("rbc_news|date").text().isEmpty()
                            ? item.select("pubDate").text()
                            : item.select("rbc_news|date").text();

            String category = item.select("category").text();

            List<String> images = item.select("rbc_news|thumbnail url")
                    .stream()
                    .map(Element::text)
                    .toList();

            result.add(new Article(
                    title,
                    shortDesc,
                    fullText,
                    link,
                    date,
                    category,
                    "RBC",
                    images
            ));
        }
        return result;
    }
}
