package org.example.consoleUI.Menu;

public class ClientMenuConsole {
    private final String[] clientIndividualMenu = new String[]{
            "=== Частные лица ===",
            "<- Назад",
            "Найти клиента ->",
            "Удалить клиента ->",
            "Создать клиента ->"
    };

    private final String[] searchClientIndividualMenu = new String[]{
            "=== Найти клиента ===",
            "<- Назад",
            "Поиск по Ф.И.О. ->",
            "Поиск по номеру телефона ->",
            "Поиск по VIN ->"
    };

    private final String[] clientLegalEntityMenu = new String[]{
            "=== Юридическое лицо ===",
            "<- Назад",
            "Найти клиента ->",
            "Создать клиента ->",
            "Создать водителя ->"
    };

    private final String[] searchClientLegalEntityMenu = new String[]{
            "=== Найти клиента ===",
            "<- Назад",
            "Поиск по Ф.И.О. ->",
            "Поиск по названию Юр.Лица ->",
            "Поиск по номеру телефона ->",
            "Поиск по VIN ->",
            "Поиск водителя ->"
    };



    public String[] getClientIndividualMenu(){
        return clientIndividualMenu;
    }
    public String[] getClientLegalEntityMenu(){
        return clientLegalEntityMenu;
    }
    public String[] getSearchClientIndividualMenu(){
        return searchClientIndividualMenu;
    }
    public String[] getSearchClientLegalEntityMenu(){
            return searchClientLegalEntityMenu;
            }
}
