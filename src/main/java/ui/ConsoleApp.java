package ui;

import analyzer.StatisticsAnalyzer;
import db.ArticleDb;
import db.Filter;
import export.NewsExporter;
import model.Article;
import service.CleanupService;
import service.ChangeLogger;
import service.NewsService;
import service.ScheduledUpdater;

import java.util.List;
import java.util.Scanner;

public class ConsoleApp {

    private final Scanner scanner;
    private final Filter filter;
    private final NewsService service;
    private final NewsExporter exporter;
    private final ScheduledUpdater updater;
    private final ArticleDb db;

    public ConsoleApp(Scanner scanner, Filter filter, NewsService service, ScheduledUpdater updater, ArticleDb db) {
        this.scanner = scanner;
        this.filter = filter;
        this.service = service;
        this.exporter = new NewsExporter();
        this.updater = updater;
        this.db = db;
    }

    public void start() throws Exception {

        while (true) {
            Thread.sleep(1000);
            showMenu();

            System.out.print("Выберите команду: ");
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1" -> print(filter.sortByDate());
                case "2" -> {
                    System.out.print("Введите ключевое слово: ");
                    print(filter.search(scanner.nextLine()));
                }
                case "3" -> {
                    System.out.print("Введите категорию: ");
                    print(filter.filterByCategory(scanner.nextLine()));
                }
                case "4" -> {
                    System.out.print("Введите дату (например 2026-05-05): ");
                    print(filter.filterByDate(scanner.nextLine()));
                }
                case "5" -> {
                    System.out.print("Введите источник: ");
                    print(filter.filterBySource(scanner.nextLine()));
                }
                case "6" -> showSortMenu();
                case "7" -> service.updateNews();
                case "8" -> exportMenu();
                case "9" -> showStatistics();
                case "10" -> ChangeLogger.showHistory();
                case "11" -> showTrendByKeyword();
                case "12" -> deleteOldNews();
                case "0" -> {
                    if (updater != null) {
                        updater.stop();
                    }
                    System.out.println("Выход...");
                    return;
                }
                default -> System.out.println("Неверная команда");
            }
        }
    }

    private void showMenu() {
        System.out.println("\n==============================");
        System.out.println("   АГРЕГАТОР НОВОСТЕЙ");
        System.out.println("==============================");
        System.out.println("1) Все новости (по дате)");
        System.out.println("2) Поиск по слову");
        System.out.println("3) Фильтр по категории");
        System.out.println("4) Фильтр по дате");
        System.out.println("5) Фильтр по источнику");
        System.out.println("6) Сортировки");
        System.out.println("7) Обновить новости");
        System.out.println("8) Экспорт данных");
        System.out.println("9) СТАТИСТИКА");
        System.out.println("10) История изменений");
        System.out.println("11) Динамика по слову");
        System.out.println("12) Удалить старые");
        System.out.println("0) Выход");
        System.out.println("==============================");
    }

    private void exportMenu() throws Exception {
        System.out.println("\n--- ЭКСПОРТ ДАННЫХ ---");
        System.out.println("1) Экспорт в JSON");
        System.out.println("2) Экспорт в CSV");
        System.out.println("0) Назад");

        System.out.print("Выберите: ");
        String choice = scanner.nextLine().trim();

        if (choice.equals("0")) return;

        List<Article> articles = filter.sortByDate();

        if (articles.isEmpty()) {
            System.out.println("Нет новостей для экспорта");
            return;
        }

        String filename = "news_" + System.currentTimeMillis();

        switch (choice) {
            case "1" -> exporter.exportToJson(articles, filename + ".json");
            case "2" -> exporter.exportToCsv(articles, filename + ".csv");
            default -> System.out.println("Неверный выбор");
        }
    }

    private void showSortMenu() throws Exception {
        System.out.println("\n--- СОРТИРОВКИ ---");
        System.out.println("1) По дате");
        System.out.println("2) По источнику");
        System.out.println("3) По популярности");
        System.out.print("Выберите: ");

        String choice = scanner.nextLine();

        List<Article> result = switch (choice) {
            case "1" -> filter.sortByDate();
            case "2" -> filter.sortBySource();
            case "3" -> filter.sortByPopularity();
            default -> {
                System.out.println("Неверный выбор");
                yield null;
            }
        };

        if (result != null) print(result);
    }

    private void print(List<Article> articles) {
        if (articles == null || articles.isEmpty()) {
            System.out.println("Ничего не найдено");
            return;
        }

        for (Article a : articles) {
            System.out.println("\n" + a.title());
            System.out.println("Категория: " + a.category());
            System.out.println("Кратко: " + a.shortDescription());
            System.out.println("Текст: " + a.fullText());
            System.out.println("Ссылка: " + a.link());
            System.out.println("Дата: " + a.date());
            System.out.println("Источник: " + a.source());
            System.out.println("----------------------------------");
        }
    }

    private void showStatistics() throws Exception {
        List<Article> articles = filter.sortByDate();
        StatisticsAnalyzer analyzer = new StatisticsAnalyzer();
        analyzer.printFullStatistics(articles);
    }

    private void showTrendByKeyword() throws Exception {
        System.out.print("Введите ключевое слово: ");
        String keyword = scanner.nextLine();
        System.out.print("За сколько дней (7/14/30): ");
        int days = Integer.parseInt(scanner.nextLine());

        List<Article> articles = filter.sortByDate();
        StatisticsAnalyzer analyzer = new StatisticsAnalyzer();
        var trend = analyzer.getTrendByKeyword(articles, keyword, days);

        System.out.println("\nДИНАМИКА УПОМИНАНИЙ \"" + keyword + "\":");
        for (var entry : trend.entrySet()) {
            String bar = "-".repeat(Math.min(entry.getValue(), 20));
            System.out.printf("   %s : %2d %s\n", entry.getKey(), entry.getValue(), bar);
        }
    }

    private void deleteOldNews() throws Exception {
        System.out.print("Удалить новости старше N дней: ");
        int days = Integer.parseInt(scanner.nextLine());
        CleanupService cleanup = new CleanupService(db);
        cleanup.deleteOlderThan(days);
    }
}