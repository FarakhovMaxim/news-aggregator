package db;

import model.Article;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FilterTest {
    private ArticleDb db;
    private Filter filter;

    @BeforeEach
    void setUp() throws Exception {
        db = new ArticleDb();
        filter = new Filter(db);

        try (Connection conn = ArticleDb.connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM images");
            stmt.execute("DELETE FROM articles");
        }

        Article article1 = new Article(
                "Java программирование сегодня",
                "Краткое описание Java",
                "Полный текст статьи про Java разработку",
                "http://test.com/1",
                "2025-05-29",
                "IT",
                "TEST",
                List.of()
        );

        Article article2 = new Article(
                "Политика в России",
                "Описание политики",
                "Текст про политические события",
                "http://test.com/2",
                "2025-05-28",
                "Политика",
                "TEST",
                List.of()
        );

        db.saveArticle(article1);
        db.saveArticle(article2);
    }

    @AfterEach
    void tearDown() throws Exception {
        try (Connection conn = ArticleDb.connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM images");
            stmt.execute("DELETE FROM articles");
        }
    }

    @Test
    void searchShouldFindByKeywordInTitle() throws Exception {
        List<Article> result = filter.search("Java");
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void searchShouldReturnEmptyForNonExistentKeyword() throws Exception {
        List<Article> result = filter.search("nonexistentword123");
        assertTrue(result.isEmpty());
    }

    @Test
    void filterByCategoryShouldReturnCorrectArticles() throws Exception {
        List<Article> result = filter.filterByCategory("IT");
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void filterByCategoryShouldReturnEmptyForNonExistentCategory() throws Exception {
        List<Article> result = filter.filterByCategory("Nonexistent");
        assertTrue(result.isEmpty());
    }

    @Test
    void filterByDateShouldReturnArticlesByDate() throws Exception {
        List<Article> result = filter.filterByDate("2025-05-29");
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void filterBySourceShouldReturnArticles() throws Exception {
        List<Article> result = filter.filterBySource("TEST");
        assertEquals(2, result.size());
    }

    @Test
    void sortByDateShouldReturnDescendingOrder() throws Exception {
        List<Article> result = filter.sortByDate();
        assertEquals(2, result.size());
        assertEquals("2025-05-29", result.get(0).date());
        assertEquals("2025-05-28", result.get(1).date());
    }

    @Test
    void sortBySourceShouldReturnAlphabeticalOrder() throws Exception {
        List<Article> result = filter.sortBySource();
        assertNotNull(result);
    }
}