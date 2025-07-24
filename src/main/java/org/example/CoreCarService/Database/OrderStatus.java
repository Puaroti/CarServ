package org.example.CoreCarService.Database;

public enum OrderStatus {
    NEW("Новый", "Заказ только создан"),
    PROCESSING("В обработке", "Мастер начал работу над заказом"),
    COMPLETED("Завершён", "Работа по заказу завершена"),
    CANCELLED("Отменён", "Заказ был отменён клиентом");


private final String title;
private final String description;

OrderStatus(String title, String description) {
    this.title = title;
    this.description = description;
}

public String getTitle() {
    return title;
}
public String getDescription() {
    return description;
}
}