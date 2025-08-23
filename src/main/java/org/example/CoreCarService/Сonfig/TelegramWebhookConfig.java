package org.example.CoreCarService.Сonfig;

import org.example.CoreCarService.Service.TelegramBot;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.api.methods.updates.DeleteWebhook;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class TelegramWebhookConfig {

    @Bean
    public ApplicationRunner registerTelegramLongPolling(TelegramBot bot) {
        return args -> {
            try {
                TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
                // Ensure webhook is disabled so Telegram will deliver updates via getUpdates
                try {
                    bot.execute(new DeleteWebhook());
                } catch (TelegramApiException ignored) {
                    // ignore if no webhook set
                }
                // Register bot in Long Polling mode (no webhook)
                botsApi.registerBot(bot);
            } catch (TelegramApiException e) {
                // Log and rethrow if needed
                throw new RuntimeException("Failed to register Telegram long polling bot: " + e.getMessage(), e);
            }
        };
    }
}
