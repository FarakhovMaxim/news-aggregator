package analyzer;

import model.Article;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StatisticsAnalyzerTest {
    private StatisticsAnalyzer analyzer;
    private List<Article> articles;

    @BeforeEach
    void setUp() {
        analyzer = new StatisticsAnalyzer();

        articles = List.of(
                new Article("Java новости программирование", "Описание Java", "Java текст программы разработка",
                        "link1", "2025-05-29", "IT", "RBC", List.of()),
                new Article("Политика РФ", "Описание политики", "Текст про политику",
                        "link2", "2025-05-28", "Политика", "LENTA", List.of()),
                new Article("Spring boot Java", "Spring описание", "Spring framework Java код",
                        "link3", "2025-05-27", "IT", "RBC", List.of())
        );
    }

    @Test
    void countByCategoryShouldReturnCorrectCounts() {
        Map<String, Integer> result = analyzer.countByCategory(articles);

        assertEquals(2, result.size());
        assertEquals(2, result.get("IT"));
        assertEquals(1, result.get("Политика"));
    }

    @Test
    void countByCategoryShouldHandleNullCategory() {
        Article article = new Article("Title", "Desc", "Text", "link", "date",
                null, "source", List.of());
        Map<String, Integer> result = analyzer.countByCategory(List.of(article));

        assertTrue(result.containsKey("Без категории"));
        assertEquals(1, result.get("Без категории"));
    }

    @Test
    void getTopWordsShouldReturnTopNWords() {
        Map<String, Integer> result = analyzer.getTopWords(articles, 5);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.size() <= 5);
    }

    @Test
    void getTopWordsShouldFilterStopWords() {
        List<Article> stopWordTest = List.of(
                new Article("это то что", "описание", "это то что для",
                        "link", "date", "cat", "src", List.of())
        );
        Map<String, Integer> result = analyzer.getTopWords(stopWordTest, 10);

        // Стоп-слова не должны попасть в результат
        for (String word : List.of("это", "то", "что", "для")) {
            assertFalse(result.containsKey(word), "Стоп-слово не должно быть: " + word);
        }
    }

    @Test
    void getTrendByKeywordShouldReturnDailyStats() {
        Map<String, Integer> result = analyzer.getTrendByKeyword(articles, "Java", 7);

        assertNotNull(result);
        assertEquals(8, result.size()); // 7 дней назад + сегодня = 8 дней
    }

    @Test
    void getTrendByKeywordShouldReturnZeroForNonExistentKeyword() {
        Map<String, Integer> result = analyzer.getTrendByKeyword(articles, "Nonexistent123", 7);
        assertNotNull(result);
        boolean allZero = result.values().stream().allMatch(count -> count == 0);
        assertTrue(allZero);
    }

    @Test
    void getTopWordsShouldHandleEmptyList() {
        Map<String, Integer> result = analyzer.getTopWords(List.of(), 5);
        assertTrue(result.isEmpty());
    }

    @Test
    void countByCategoryShouldHandleEmptyList() {
        Map<String, Integer> result = analyzer.countByCategory(List.of());
        assertTrue(result.isEmpty());
    }
}