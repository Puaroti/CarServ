package org.example.consoleUI.AuthRegMenu;

public class AuthRegMenu {
    private final String[] mainMenu = new String[]{
            "=== Главное меню ===",
            "Регистрация(тестирования ядра регистрации) ->",
            "Авторизация ->",
            "О программе/помощь ->"
    };
    private final String[] regMenu = new String[]{
            "=== Регистрация(тестирования ядра регистрации) ===",
            "Логин должен содержать от 6-10 знаков латинского алфавита, цифр. ",
            "Введите логин:",
            "Повторите ввод логина:",
            "Пароль должен содержать от 6-15 знаков латинского алфавита, строчные и прописные буквы, цифры и знаки !?%()<>*$#{}",
            "Введите пароль:",
            "Повторите ввод пароля",
            "Введите номер телефона для связи:",
            "Введите ФИО:",
            "Введите EMAIL:"
    };
    private final String[] authMenu = new String[]{
            "=== Авторизация ===",
            "Введите ваш логин:",
            "Введите ваш пароль:"
    };
    private final String[] helpMenu = new String[]{
            "=== какая то хуйня ==="
    };

    public String[] getMainMenu() {
        return mainMenu;
    }

    public String[] getRegMenu() {
        return regMenu;
    }

    public String[] getAuthMenu() {
        return authMenu;
    }

    public String[] getHelpMenu(){
        return helpMenu;
    }

}
