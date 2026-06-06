package service;

import db.ArticleDb;
import model.Article;
import parser.Parser;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class NewsService {

    private final ArticleDb db;
    private final List<Parser> parsers;
    private ExecutorService threadPool;

    public NewsService(ArticleDb db, List<Parser> parsers) {
        this.db = db;
        this.parsers = parsers;
        this.threadPool = Executors.newFixedThreadPool(parsers.size());
    }

    public void updateNews() throws Exception {
        System.out.println("Обновляем новости параллельно...");
        long startTime = System.currentTimeMillis();

        for (Parser parser : parsers) {
            threadPool.submit(() -> {
                try {
                    List<Article> articles = parser.parse();
                    int newCount = 0;
                    for (Article article : articles) {
                        try {
                            db.saveArticle(article);
                            newCount++;
                        } catch (Exception e) {
                        }
                    }
                    if (newCount > 0) {
                        ChangeLogger.logAdd(parser.getClass().getSimpleName(), newCount);
                    }
                    System.out.println("  " + parser.getClass().getSimpleName() + ": добавлено " + newCount);
                } catch (Exception e) {
                    System.err.println("  Ошибка " + parser.getClass().getSimpleName() + ": " + e.getMessage());
                }
            });
        }

        threadPool.shutdown();
        boolean finished = threadPool.awaitTermination(2, TimeUnit.MINUTES);

        long duration = System.currentTimeMillis() - startTime;

        if (finished) {
            System.out.println("Обновление завершено за " + duration + " мс");
        } else {
            System.out.println("Обновление не завершилось вовремя");
            threadPool.shutdownNow();
        }

        threadPool = Executors.newFixedThreadPool(parsers.size());
    }

    public void shutdown() {
        if (threadPool != null && !threadPool.isShutdown()) {
            threadPool.shutdown();
        }
    }
}