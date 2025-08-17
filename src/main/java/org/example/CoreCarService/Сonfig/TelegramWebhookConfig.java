package org.example.CoreCarService.Сonfig;

import org.example.CoreCarService.Service.TelegramBot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class TelegramWebhookConfig {

    @Bean
    public ApplicationRunner registerTelegramWebhook(TelegramBot bot,
                                                     @Value("${telegram.bot.webhook-url}") String webhookUrl) {
        return args -> {
            try {
                TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
                SetWebhook setWebhook = SetWebhook.builder().url(webhookUrl).build();
                botsApi.registerBot(bot, setWebhook);
            } catch (TelegramApiException e) {
                // Log and rethrow if needed
                throw new RuntimeException("Failed to register Telegram webhook: " + e.getMessage(), e);
            }
        };
    }
}
