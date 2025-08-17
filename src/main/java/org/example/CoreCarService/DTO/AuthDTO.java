package org.example.CoreCarService.DTO;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class AuthDTO {
    private String login;
    private LocalDateTime authenticationDate;
    private Map<String, String> data =  new HashMap<>(3);


    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public LocalDateTime getAuthenticationDate() {
        return authenticationDate;
    }

    public void setAuthenticationDate(LocalDateTime authenticationDate) {
        this.authenticationDate = authenticationDate;
    }

    public Map<String, String> getANDsetData() {
        data.put("login", login );
        data.put("authenticationDate", authenticationDate.toString());
        return data;
    }

}
