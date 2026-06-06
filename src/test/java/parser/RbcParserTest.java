package parser;

import http.HttpFetcher;
import model.Article;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RbcParserTest {

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
                    <title>РБК тест новость</title>
                    <link>https://www.rbc.ru/test/1</link>
                    <description>Обычное описание</description>
                    <rbc_news:anons>Анонс новости РБК</rbc_news:anons>
                    <rbc_news:full-text>Полный текст статьи РБК с деталями</rbc_news:full-text>
                    <rbc_news:date>2025-05-29</rbc_news:date>
                    <category>Экономика</category>
                    <rbc_news:thumbnail>
                        <url>https://www.rbc.ru/image.jpg</url>
                    </rbc_news:thumbnail>
                </item>
            </channel>
        </rss>
        """;

    @Test
    void parseShouldReturnListOfArticles() throws Exception {
        HttpFetcher fetcher = new MockHttpFetcher(sampleRss);
        RbcParser parser = new RbcParser(fetcher);

        List<Article> result = parser.parse();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void parseShouldExtractCorrectArticleData() throws Exception {
        HttpFetcher fetcher = new MockHttpFetcher(sampleRss);
        RbcParser parser = new RbcParser(fetcher);

        List<Article> result = parser.parse();
        Article article = result.get(0);

        assertEquals("РБК тест новость", article.title());
        assertEquals("Анонс новости РБК", article.shortDescription());
        assertEquals("Полный текст статьи РБК с деталями", article.fullText());
        assertEquals("https://www.rbc.ru/test/1", article.link());
        assertEquals("2025-05-29", article.date());
        assertEquals("Экономика", article.category());
        assertEquals("RBC", article.source());
    }

    @Test
    void parseShouldExtractImageUrls() throws Exception {
        HttpFetcher fetcher = new MockHttpFetcher(sampleRss);
        RbcParser parser = new RbcParser(fetcher);

        List<Article> result = parser.parse();
        Article article = result.get(0);

        assertNotNull(article.images());
        assertEquals(1, article.images().size());
        assertEquals("https://www.rbc.ru/image.jpg", article.images().get(0));
    }

    @Test
    void parseShouldUseDescriptionWhenAnonsMissing() throws Exception {
        String rssWithoutAnons = """
            <?xml version="1.0" encoding="UTF-8"?>
            <rss>
                <channel>
                    <item>
                        <title>Новость без анонса</title>
                        <link>https://www.rbc.ru/test/2</link>
                        <description>Обычное описание description</description>
                        <category>Спорт</category>
                    </item>
                </channel>
            </rss>
            """;

        HttpFetcher fetcher = new MockHttpFetcher(rssWithoutAnons);
        RbcParser parser = new RbcParser(fetcher);

        List<Article> result = parser.parse();
        Article article = result.get(0);

        assertEquals("Обычное описание description", article.shortDescription());
    }

    @Test
    void parseShouldUsePubDateWhenRbcDateMissing() throws Exception {
        String rssWithoutDate = """
            <?xml version="1.0" encoding="UTF-8"?>
            <rss>
                <channel>
                    <item>
                        <title>Новость без даты</title>
                        <link>https://www.rbc.ru/test/3</link>
                        <pubDate>2025-05-30</pubDate>
                    </item>
                </channel>
            </rss>
            """;

        HttpFetcher fetcher = new MockHttpFetcher(rssWithoutDate);
        RbcParser parser = new RbcParser(fetcher);

        List<Article> result = parser.parse();
        Article article = result.get(0);

        assertEquals("2025-05-30", article.date());
    }

    @Test
    void parseShouldSkipDuplicateLinks() throws Exception {
        String rssWithDuplicate = """
            <?xml version="1.0" encoding="UTF-8"?>
            <rss>
                <channel>
                    <item>
                        <title>Первая новость</title>
                        <link>https://www.rbc.ru/test/same</link>
                    </item>
                    <item>
                        <title>Вторая новость</title>
                        <link>https://www.rbc.ru/test/same</link>
                    </item>
                </channel>
            </rss>
            """;

        HttpFetcher fetcher = new MockHttpFetcher(rssWithDuplicate);
        RbcParser parser = new RbcParser(fetcher);

        List<Article> result = parser.parse();

        assertEquals(1, result.size());
        assertEquals("Первая новость", result.get(0).title());
    }
}