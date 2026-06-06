package ui;

public enum Command {
    SHOW_ALL("Показать все новости"),
    SEARCH("Поиск по ключевому слову"),
    CATEGORY("Фильтр по категории"),
    SORT("Сортировка по дате"),
    UPDATE("Обновить новости"),
    EXIT("Выход");

    private final String title;

    Command(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
