package export;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.Article;
import java.io.FileWriter;
import java.util.List;

public class NewsExporter {

    public void exportToJson(List<Article> articles, String filename) throws Exception {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(articles);
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(json);
        }
        System.out.println("Экспортировано " + articles.size() + " новостей в " + filename);
    }

    public void exportToCsv(List<Article> articles, String filename) throws Exception {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("Заголовок,Категория,Источник,Дата,Ссылка,Краткое описание\n");
            for (Article a : articles) {
                writer.write(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                        escapeCsv(a.title()),
                        escapeCsv(a.category()),
                        escapeCsv(a.source()),
                        escapeCsv(a.date()),
                        escapeCsv(a.link()),
                        escapeCsv(a.shortDescription())
                ));
            }
        }

        System.out.println("Экспортировано " + articles.size() + " новостей в " + filename);
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }
}