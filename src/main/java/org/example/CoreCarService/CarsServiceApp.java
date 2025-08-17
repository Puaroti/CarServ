package org.example.CoreCarService;
import jakarta.annotation.PostConstruct;
import org.example.CoreCarService.Service.TelegramBot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
public class CarsServiceApp {
        public static void main(String[] args) {
            SpringApplication.run(CarsServiceApp.class, args);
        }

    }

