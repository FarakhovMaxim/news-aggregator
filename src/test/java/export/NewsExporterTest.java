package export;

import model.Article;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NewsExporterTest {
    private NewsExporter exporter;
    private List<Article> articles;
    private String testJsonFile;
    private String testCsvFile;

    @BeforeEach
    void setUp() {
        exporter = new NewsExporter();
        testJsonFile = "test_export.json";
        testCsvFile = "test_export.csv";

        articles = List.of(
                new Article("Test Title", "Short desc", "Full text",
                        "http://test.com", "2025-05-29", "IT", "RBC", List.of())
        );
    }

    @AfterEach
    void tearDown() {
        new File(testJsonFile).delete();
        new File(testCsvFile).delete();
    }

    @Test
    void exportToJsonShouldCreateFile() throws Exception {
        exporter.exportToJson(articles, testJsonFile);

        File file = new File(testJsonFile);
        assertTrue(file.exists());
        assertTrue(file.length() > 0);
    }

    @Test
    void exportToJsonShouldContainArticleData() throws Exception {
        exporter.exportToJson(articles, testJsonFile);

        String content = Files.readString(new File(testJsonFile).toPath());
        assertTrue(content.contains("Test Title"));
        assertTrue(content.contains("IT"));
        assertTrue(content.contains("RBC"));
    }

    @Test
    void exportToCsvShouldCreateFile() throws Exception {
        exporter.exportToCsv(articles, testCsvFile);

        File file = new File(testCsvFile);
        assertTrue(file.exists());
        assertTrue(file.length() > 0);
    }

    @Test
    void exportToCsvShouldContainHeaders() throws Exception {
        exporter.exportToCsv(articles, testCsvFile);

        String content = Files.readString(new File(testCsvFile).toPath());
        assertTrue(content.contains("Заголовок"));
        assertTrue(content.contains("Категория"));
        assertTrue(content.contains("Источник"));
    }

    @Test
    void exportToCsvShouldEscapeQuotes() throws Exception {
        Article articleWithQuotes = new Article(
                "Title with \"quotes\"", "Desc", "Text",
                "link", "date", "cat", "src", List.of());

        exporter.exportToCsv(List.of(articleWithQuotes), testCsvFile);

        String content = Files.readString(new File(testCsvFile).toPath());
        assertTrue(content.contains("\"\"quotes\"\""));
    }
}