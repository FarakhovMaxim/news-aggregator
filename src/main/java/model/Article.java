package model;

import java.util.List;

public record Article(
        String title,
        String shortDescription,
        String fullText,
        String link,
        String date,
        String category,
        String source,
        List<String> images
) {}