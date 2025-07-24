package org.example.consoleUI.Menu;

public class MainMenuConsole {

    private final String[] mainMenu = new String[]{
            "=== Главное меню ===",
            "<- Выход",
            "Клиенты ->",
            "Заказ-наряды ->",
            "Мастера ->",
            "Рабочее время ->",
            "Зарплаты ->"
    };
    private final String[] clientMenu = new String[]{
            "=== Клиенты ===",
            "<- Назад",
            "Частное лицо ->",
            "Юридическое лицо ->"
    };
    private final String[] ordersMenu = new String[]{
            "=== Заказ-наряды ===",
            "<- Назад",
            "Найти заказ-наряд по номеру заказа->",
            "Найти заказ-наряд по времени создания ->",
            "Создать заказ-наряд ->"
    };
    private final String[] masterMenu = new String[]{
            "=== Мастера ===",
            "<- Назад",
            "Найти мастера ->",
            "Вывести список всех мастеров ->",
            "Заблокировать/разблокировать мастера ->",
            "Создать мастера ->",
            "Удалить мастера ->"
    };
    private final String[] workTimeMenu = new String[]{
            "=== Рабочее время ===",
            "<- Назад",
            "Вывести таблицу графиков рабочего времени ->",
            "Вывести таблицу записей на сервис ->",
    };
    private final String[] financeMenu = new String[]{
            "=== Финансы ===",
            "<- Назад",
            "Долги ->",
            "Зарплаты ->",
            "Прибыль ->",
            "Должники ->"
    };

    public String[] getMainMenu(){
        return mainMenu;
    }
    public String[] getOrdersMenu(){
        return ordersMenu;
    }

    public String[] getClientMenu() {
        return clientMenu;
    }

    public String[] getMasterMenu(){
        return masterMenu;
    }

    public String[] getWorkTimeMenu() {
        return workTimeMenu;
    }

    public String[] getFinanceMenu() {
        return financeMenu;
    }
}
