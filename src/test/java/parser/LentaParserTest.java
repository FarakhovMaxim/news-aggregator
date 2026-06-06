package parser;

import http.HttpFetcher;
import model.Article;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LentaParserTest {

    private static class MockHttpFetcher extends HttpFetcher {
        private final String response;

        MockHttpFetcher(String response) {
            this.response = response;
        }

        @Override
        public String get(String url) {
            return response;
        }
    }

    private final String sampleRss = """
        <?xml version="1.0" encoding="UTF-8"?>
        <rss>
            <channel>
                <item>
                    <title>Тестовая новость Lenta</title>
                    <link>https://lenta.ru/test/1</link>
                    <description>Краткое описание новости</description>
                    <pubDate>2025-05-29</pubDate>
                    <category>Общество</category>
                    <enclosure url="https://lenta.ru/image.jpg" type="image/jpeg"/>
                </item>
                <item>
                    <title>Вторая новость</title>
                    <link>https://lenta.ru/test/2</link>
                    <description>Описание второй</description>
                    <pubDate>2025-05-28</pubDate>
                    <category>Политика</category>
                </item>
            </channel>
        </rss>
        """;

    @Test
    void parseShouldReturnListOfArticles() throws Exception {
        HttpFetcher fetcher = new MockHttpFetcher(sampleRss);
        LentaParser parser = new LentaParser(fetcher);

        List<Article> result = parser.parse();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void parseShouldExtractCorrectArticleData() throws Exception {
        HttpFetcher fetcher = new MockHttpFetcher(sampleRss);
        LentaParser parser = new LentaParser(fetcher);

        List<Article> result = parser.parse();
        Article first = result.get(0);

        assertEquals("Тестовая новость Lenta", first.title());
        assertEquals("Краткое описание новости", first.shortDescription());
        assertEquals("https://lenta.ru/test/1", first.link());
        assertEquals("2025-05-29", first.date());
        assertEquals("Общество", first.category());
        assertEquals("LENTA", first.source());
    }

    @Test
    void parseShouldExtractImageUrl() throws Exception {
        HttpFetcher fetcher = new MockHttpFetcher(sampleRss);
        LentaParser parser = new LentaParser(fetcher);

        List<Article> result = parser.parse();
        Article first = result.get(0);

        assertNotNull(first.images());
        assertEquals(1, first.images().size());
        assertEquals("https://lenta.ru/image.jpg", first.images().get(0));
    }

    @Test
    void parseShouldSkipDuplicateLinks() throws Exception {
        String rssWithDuplicate = """
            <?xml version="1.0" encoding="UTF-8"?>
            <rss>
                <channel>
                    <item>
                        <title>Первая новость</title>
                        <link>https://lenta.ru/test/same</link>
                    </item>
                    <item>
                        <title>Вторая новость</title>
                        <link>https://lenta.ru/test/same</link>
                    </item>
                </channel>
            </rss>
            """;

        HttpFetcher fetcher = new MockHttpFetcher(rssWithDuplicate);
        LentaParser parser = new LentaParser(fetcher);

        List<Article> result = parser.parse();

        assertEquals(1, result.size());
        assertEquals("Первая новость", result.get(0).title());
    }

    @Test
    void parseShouldHandleEmptyDescription() throws Exception {
        String rssWithEmptyDesc = """
            <?xml version="1.0" encoding="UTF-8"?>
            <rss>
                <channel>
                    <item>
                        <title>Новость без описания</title>
                        <link>https://lenta.ru/test/empty</link>
                        <description></description>
                        <pubDate>2025-05-29</pubDate>
                    </item>
                </channel>
            </rss>
            """;

        HttpFetcher fetcher = new MockHttpFetcher(rssWithEmptyDesc);
        LentaParser parser = new LentaParser(fetcher);

        List<Article> result = parser.parse();
        Article article = result.get(0);

        assertEquals("отсутсвует", article.shortDescription());
    }
}