package org.example.CoreCarService.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@RestController
@RequestMapping("${telegram.bot.webhook-path:/telegram}")
public class TelegramWebhookController {

    private final TelegramBot bot;
    private static final Logger log = LoggerFactory.getLogger(TelegramWebhookController.class);

    public TelegramWebhookController(TelegramBot bot) {
        this.bot = bot;
    }

    @GetMapping
    public ResponseEntity<String> probe() {
        return ResponseEntity.ok("telegram webhook OK");
    }

    @PostMapping
    public ResponseEntity<Void> onUpdate(@RequestBody Update update) {
        try {
            log.info("Incoming Telegram update: {}", update.getUpdateId());
            BotApiMethod<?> method = bot.onWebhookUpdateReceived(update);
            if (method != null) {
                bot.execute(method);
            }
            return ResponseEntity.ok().build();
        } catch (TelegramApiException e) {
            log.error("Failed to process Telegram update", e);
            return ResponseEntity.status(500).build();
        }
    }
}
