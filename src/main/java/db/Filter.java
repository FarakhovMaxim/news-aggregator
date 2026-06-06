package db;

import model.Article;

import java.util.List;

public class Filter {

    private final ArticleDb db;

    public Filter(ArticleDb db) {
        this.db = db;
    }

    public List<Article> search(String keyword) throws Exception {
        return db.query("""
            SELECT * FROM articles
            WHERE LOWER(title) LIKE LOWER(?)
               OR LOWER(full_text) LIKE LOWER(?)
        """, "%" + keyword + "%", "%" + keyword + "%");
    }

    public List<Article> filterByCategory(String category) throws Exception {
        return db.query("SELECT * FROM articles WHERE category = ?", category);
    }

    public List<Article> filterBySource(String source) throws Exception {
        return db.query("SELECT * FROM articles WHERE source = ?", source);
    }

    // 🆕 ФИЛЬТР ПО ДАТЕ
    public List<Article> filterByDate(String date) throws Exception {
        return db.query("SELECT * FROM articles WHERE date = ?", date);
    }

    public List<Article> sortByDate() throws Exception {
        return db.query("SELECT * FROM articles ORDER BY date DESC");
    }

    public List<Article> sortBySource() throws Exception {
        return db.query("SELECT * FROM articles ORDER BY source ASC");
    }

    public List<Article> sortByPopularity() throws Exception {
        return db.query("SELECT * FROM articles ORDER BY LENGTH(full_text) DESC");
    }
}