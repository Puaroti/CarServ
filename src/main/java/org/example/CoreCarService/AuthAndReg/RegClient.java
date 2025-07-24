package org.example.CoreCarService.AuthAndReg;

import java.util.regex.Pattern;
import com.password4j.Password;

import org.example.CoreCarService.Database.DatabaseManager;

public class RegClient {
    private String hashedPassword;
    private final String PASSWORD_REGEX =
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*]).{8,}$"; //Проверки валидности
    private final String LOGIN_REGEX =
            "^[a-zA-Z0-9_]{3,20}$";
    private final String NAME_REGEX =
            "^[a-zA-Z0-9_]{3,20}$";


    public String hashedPassword(String password) {
        hashedPassword = Password.hash(password).withBcrypt().getResult();
        return hashedPassword;
    }
    public boolean isPasswordValid (String password) {
        return Pattern.matches(PASSWORD_REGEX, password);
    }
    public boolean isLoginValid (String login) {
        return Pattern.matches(LOGIN_REGEX, login);
    }
    public boolean isNameValid (String name) {
        return Pattern.matches(NAME_REGEX, name);
    }
    public boolean checkLogin (String login) {
        DatabaseManager dm = new DatabaseManager();
        return dm.isLoginExists(login);
    }
}
