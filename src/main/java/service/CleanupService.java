package service;

import db.ArticleDb;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CleanupService {
    private final ArticleDb db;

    public CleanupService(ArticleDb db) {
        this.db = db;
    }

    public void deleteOlderThan(int days) throws Exception {
        LocalDate cutoff = LocalDate.now().minusDays(days);
        String cutoffStr = cutoff.format(DateTimeFormatter.ISO_LOCAL_DATE);

        String sql = "DELETE FROM articles WHERE date < ?";
        try (var conn = ArticleDb.connect();
             var ps = conn.prepareStatement(sql)) {

            ps.setString(1, cutoffStr);
            int deleted = ps.executeUpdate();

            if (deleted > 0) {
                ChangeLogger.logDelete(deleted, cutoffStr);
                System.out.println("🗑️ Удалено " + deleted + " старых новостей (старше " + days + " дней)");
            }
        }
    }
}