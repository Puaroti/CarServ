package org.example.CoreCarService.AuthAndReg;

import java.sql.SQLException;

public class AuthAndRegService {

    public void youRegOrAuth(String status){
        switch(status){
            case "auth":
                authClient();
                break;
                case "reg":
                regClient();
                break;
        }
    }
    private void authClient(){

    }

    private void regClient(){

    }

}
