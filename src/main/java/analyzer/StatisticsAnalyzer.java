package analyzer;

import model.Article;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class StatisticsAnalyzer {
    public Map<String, Integer> countByCategory(List<Article> articles) {
        Map<String, Integer> stats = new HashMap<>();
        for (Article a : articles) {
            String cat = a.category() == null || a.category().isEmpty() ? "Без категории" : a.category();
            stats.put(cat, stats.getOrDefault(cat, 0) + 1);
        }
        return stats;
    }

    public Map<String, Integer> getTopWords(List<Article> articles, int topN) {
        Map<String, Integer> wordCount = new HashMap<>();
        List<String> stopWords = List.of(
                "это", "все", "так", "было", "что", "на", "и", "в", "с", "к", "у", "по", "за", "из", "о",
                "как", "для", "же", "но", "еще", "уже", "без", "до", "при", "только", "если", "чтобы"
        );

        for (Article a : articles) {
            String text = (a.title() + " " + a.fullText() + " " + a.shortDescription()).toLowerCase();
            String[] words = text.split("[^а-яА-Яa-zA-Z]+");

            for (String w : words) {
                if (w.length() > 3 && !stopWords.contains(w)) {
                    wordCount.put(w, wordCount.getOrDefault(w, 0) + 1);
                }
            }
        }

        return wordCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(topN)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
    }

    public Map<String, Integer> getTrendByKeyword(List<Article> articles, String keyword, int daysBack) {
        Map<String, Integer> dailyStats = new LinkedHashMap<>();
        LocalDate now = LocalDate.now();
        for (int i = daysBack; i >= 0; i--) {
            LocalDate day = now.minusDays(i);
            dailyStats.put(day.toString(), 0);
        }

        for (Article a : articles) {
            String content = (a.title() + " " + a.fullText()).toLowerCase();
            if (content.contains(keyword.toLowerCase())) {
                String date = a.date();
                if (date != null && date.length() >= 10) {
                    String day = date.substring(0, 10);
                    if (dailyStats.containsKey(day)) {
                        dailyStats.put(day, dailyStats.get(day) + 1);
                    }
                }
            }
        }

        return dailyStats;
    }

    public void printFullStatistics(List<Article> articles) throws Exception {
        System.out.println("\n========== СТАТИСТИКА НОВОСТЕЙ ==========");
        System.out.println("Всего новостей: " + articles.size());

        System.out.println("\nПО КАТЕГОРИЯМ:");
        var byCategory = countByCategory(articles);
        byCategory.forEach((cat, count) ->
                System.out.printf("   %-15s : %d шт. (%d%%)\n", cat, count, count * 100 / articles.size()));

        System.out.println("\nТОП-10 УПОМИНАЕМЫХ СЛОВ:");
        var topWords = getTopWords(articles, 10);
        int rank = 1;
        for (var entry : topWords.entrySet()) {
            System.out.printf("   %d. %-15s : %d раз\n", rank++, entry.getKey(), entry.getValue());
        }

        System.out.println("\n📡 ПО ИСТОЧНИКАМ:");
        Map<String, Integer> bySource = new HashMap<>();
        for (Article a : articles) {
            String src = a.source() == null ? "Unknown" : a.source();
            bySource.put(src, bySource.getOrDefault(src, 0) + 1);
        }
        bySource.forEach((src, count) ->
                System.out.printf("   %-10s : %d шт.\n", src, count));

        System.out.println("============================================\n");
    }
}