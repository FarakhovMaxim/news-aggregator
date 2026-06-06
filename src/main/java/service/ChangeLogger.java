package service;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChangeLogger {
    private static final String LOG_FILE = "news_history.log";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void log(String action, String details) {
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            String entry = String.format("[%s] %s: %s",
                    LocalDateTime.now().format(FORMATTER),
                    action,
                    details);
            pw.println(entry);

        } catch (IOException e) {
            System.err.println("Не удалось записать лог: " + e.getMessage());
        }
    }

    public static void logAdd(String source, int count) {
        log("ДОБАВЛЕНО", source + " | " + count + " новостей");
    }

    public static void logDelete(int count, String olderThan) {
        log("УДАЛЕНО", count + " устаревших новостей (до " + olderThan + ")");
    }

    public static void showHistory() {
        System.out.println("\nИСТОРИЯ ИЗМЕНЕНИЙ (последние 20 записей):");
        System.out.println("----------------------------------------");

        try (BufferedReader reader = new BufferedReader(new FileReader(LOG_FILE))) {
            java.util.List<String> lines = reader.lines().toList();
            int start = Math.max(0, lines.size() - 20);
            for (int i = start; i < lines.size(); i++) {
                System.out.println(lines.get(i));
            }
        } catch (IOException e) {
            System.out.println("История пока пуста");
        }
        System.out.println("----------------------------------------");
    }
}