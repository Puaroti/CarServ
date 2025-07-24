package org.example.consoleUI.Menu;

import java.awt.*;
import java.util.Scanner;

public class ConsoleMenuService {
    private final Scanner scanner = new Scanner(System.in);
    private final MainMenuConsole mainMenu;
    private final ClientMenuConsole clientMenu;

    public ConsoleMenuService(MainMenuConsole mainMenu, ClientMenuConsole clientMenu) {
        this.mainMenu = mainMenu;
        this.clientMenu = clientMenu;
    }

    public void startMenu (){
        menu();
    }
    public void menu (){
        while(true){
            String[] listMenu = mainMenu.getMainMenu();
            int menuLength = listMenu.length;
            for(int i = 0; i < listMenu.length; i++){
                System.out.println(i+1 + ". " + listMenu[i]);
            }
            System.out.println("Куда идем?");
            int index = scanner.nextInt();
            if(index > menuLength || index < 0){
                System.out.println("error.");
            }
            else if(index >= 0 || index == menuLength){
                System.out.println("пЕРЕХОДИМ");
            }
        }
    }
    private final void caseMenu (int index){
        switch (index){
            case 0 -> System.out.println("!");
            case 1 -> System.out.println("!!");
            case 2 -> System.out.println("!!!");
            case 3 -> System.out.println("!!!!");
            case 4 -> System.out.println("!!!!!");
            case 5 -> System.out.println("!!!!!!");
    }
    }
}
