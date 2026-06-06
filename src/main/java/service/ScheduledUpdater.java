package service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduledUpdater {
    private final NewsService newsService;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private boolean running = false;

    public ScheduledUpdater(NewsService newsService) {
        this.newsService = newsService;
    }

    public void start(int intervalHours) {
        if (running) return;
        running = true;

        scheduler.scheduleAtFixedRate(() -> {
            try {
                System.out.println("\n [АВТООБНОВЛЕНИЕ] Загрузка новых новостей...");
                newsService.updateNews();
                System.out.println("[АВТООБНОВЛЕНИЕ] Завершено");
            } catch (Exception e) {
                System.err.println("[АВТООБНОВЛЕНИЕ] Ошибка: " + e.getMessage());
            }
        }, 0, intervalHours, TimeUnit.HOURS);

        System.out.println("⏰ Автообновление запущено (каждые " + intervalHours + " ч.)");
    }

    public void stop() {
        if (!running) return;
        scheduler.shutdown();
        running = false;
        System.out.println("⏰ Автообновление остановлено");
    }
}