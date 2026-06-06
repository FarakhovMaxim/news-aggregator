package parser;

import model.Article;

import java.util.List;

public interface Parser {
    List<Article> parse() throws Exception;
}
