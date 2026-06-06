package app;

import db.ArticleDb;
import db.Filter;
import http.HttpFetcher;
import parser.LentaParser;
import parser.Parser;
import parser.RbcParser;
import service.CleanupService;
import service.NewsService;
import service.ScheduledUpdater;
import ui.ConsoleApp;

import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {
        ArticleDb.init();
        ArticleDb db = new ArticleDb();

        HttpFetcher fetcher = new HttpFetcher();

        List<Parser> parsers = List.of(
                new RbcParser(fetcher),
                new LentaParser(fetcher)
        );

        NewsService service = new NewsService(db, parsers);

        if (db.isEmpty()) {
            System.out.println("БД пустая, загружаем новости...");
            service.updateNews();
        } else {
            System.out.println("БД уже содержит данные");
        }

        ScheduledUpdater updater = new ScheduledUpdater(service);
        updater.start(6);

        CleanupService cleanup = new CleanupService(db);
        cleanup.deleteOlderThan(30);

        Filter filter = new Filter(db);
        Scanner scanner = new Scanner(System.in);

        ConsoleApp app = new ConsoleApp(scanner, filter, service, updater, db);

        app.start();

        service.shutdown();
    }
}