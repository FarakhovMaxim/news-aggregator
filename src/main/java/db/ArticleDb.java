package db;

import model.Article;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ArticleDb {
    public static Connection connect() throws Exception {
        return DriverManager.getConnection("jdbc:sqlite:news.db");
    }

    public static void init() throws Exception {

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            String articlesTable = """
                CREATE TABLE IF NOT EXISTS articles (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT,
                    short_description TEXT,
                    full_text TEXT,
                    link TEXT UNIQUE,
                    date TEXT,
                    category TEXT,
                    source TEXT
                );
            """;

            String imagesTable = """
                CREATE TABLE IF NOT EXISTS images (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    article_id INTEGER,
                    url TEXT,
                    FOREIGN KEY (article_id) REFERENCES articles(id)
                );
            """;

            stmt.execute(articlesTable);
            stmt.execute(imagesTable);
        }
    }

    public void saveArticle(Article article) throws Exception {

        String sql = """
        INSERT OR IGNORE INTO articles(
                title,
                short_description,
                full_text,
                link,
                date,
                category,
               source
            ) VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, article.title());
            stmt.setString(2, article.shortDescription());
            stmt.setString(3, article.fullText());
            stmt.setString(4, article.link());
            stmt.setString(5, article.date());
            stmt.setString(6, article.category());
            stmt.setString(7, article.source());

            stmt.executeUpdate();

            var rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int articleId = rs.getInt(1);
                saveImages(conn, articleId, article);
            }
        }
    }

    private void saveImages(Connection conn, int articleId, Article article) throws Exception {
        if (article.images() == null) return;
        String sql = "INSERT INTO images(article_id, url) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (String url : article.images()) {
                ps.setInt(1, articleId);
                ps.setString(2, url);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public List<Article> getAllArticles() throws Exception {

        String sql = "SELECT * FROM articles";
        List<Article> result = new ArrayList<>();

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                List<String> images = getImages(conn, id);
                result.add(new Article(
                        rs.getString("title"),
                        rs.getString("short_description"),
                        rs.getString("full_text"),
                        rs.getString("link"),
                        rs.getString("date"),
                        rs.getString("category"),
                        rs.getString("source"),
                        images
                ));
            }
        }

        return result;
    }

    public List<String> getImages(Connection conn, int articleId) throws Exception {
        String sql = "SELECT url FROM images WHERE article_id = ?";
        List<String> images = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, articleId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    images.add(rs.getString("url"));
                }
            }
        }

        return images;
    }

    public List<Article> query(String sql, String... params) throws Exception {

        List<Article> result = new ArrayList<>();

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                ps.setString(i + 1, params[i]);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");

                    List<String> images = getImages(conn, id);

                    result.add(new Article(
                            rs.getString("title"),
                            rs.getString("short_description"),
                            rs.getString("full_text"),
                            rs.getString("link"),
                            rs.getString("date"),
                            rs.getString("category"),
                            rs.getString("source"),
                            images
                    ));
                }
            }
        }

        return result;
    }

    public boolean isEmpty() throws Exception {
        String sql = "SELECT COUNT(*) FROM articles";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            return rs.getInt(1) == 0;
        }
    }
}