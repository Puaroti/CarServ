package org.example.CoreCarService.Service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Небольшой контроллер для операционных проверок.
 * <p>
 * Предоставляет GET-зонд по пути, задаваемому свойством {@code telegram.bot.webhook-path}
 * (по умолчанию: <code>/telegram</code>), который возвращает простое сообщение OK. Полезно
 * для внешних health-check и проверки настройки reverse-proxy.
 * <p>
 * Примечание: сам бот сейчас работает в режиме <b>Long Polling</b>, поэтому этот контроллер
 * больше не принимает обновления Telegram и служит только для проверки доступности.
 */
@RestController
@RequestMapping("${telegram.bot.webhook-path:/telegram}")
public class TelegramWebhookController {

    private final TelegramBot bot;

    public TelegramWebhookController(TelegramBot bot) {
        this.bot = bot;
    }

    @GetMapping
    public ResponseEntity<String> probe() {
        return ResponseEntity.ok("telegram webhook OK");
    }
}
