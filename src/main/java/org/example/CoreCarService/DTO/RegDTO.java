package org.example.CoreCarService.DTO;

import java.time.LocalDateTime;

public class RegDTO {
    private String login;
    private String password;
    private UserRole role;
    private RegistrationStatus status;
    private String nameUser;
    private LocalDateTime registrationDate;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getNameUser() {
        return nameUser;
    }

    public void setNameUser(String nameUser) {
        this.nameUser = nameUser;
    }

    public LocalDateTime getRegistrationDate() {

        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }
    public  RegistrationStatus getStatus() {
        return status;
    }
    public void setStatus(RegistrationStatus status) {
        this.status = status;
    }
}
