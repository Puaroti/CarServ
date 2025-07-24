package org.example.consoleUI;

import org.example.CoreCarService.Database.DatabaseInit;
import org.example.consoleUI.Menu.ClientMenuConsole;
import org.example.consoleUI.Menu.ConsoleMenuService;
import org.example.consoleUI.Menu.MainMenuConsole;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Scanner;

public class ConsoleApp {
    private static final Scanner sc = new Scanner(System.in);
    public static void main(String[] args) throws SQLException, ClassNotFoundException, IOException, InterruptedException {
        DatabaseInit db = new DatabaseInit();
        db.initRegAuthDB();

        MainMenuConsole mainMenuConsole = new MainMenuConsole();
        ClientMenuConsole clientMenuConsole = new ClientMenuConsole();


        ConsoleMenuService consoleMenuService = new ConsoleMenuService(mainMenuConsole,  clientMenuConsole);


    }
}
