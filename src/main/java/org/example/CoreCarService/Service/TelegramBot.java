package org.example.CoreCarService.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.example.CoreCarService.Repository.RegistrationRepository;
import org.example.CoreCarService.DatabaseEntity.RegUserInfoEntity;
import org.example.CoreCarService.Repository.CarClientRepository;
import org.example.CoreCarService.DatabaseEntity.CarClientEntity;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Реализация Telegram-бота, работающего в режиме Long Polling.
 * <p>
 * Бот получает обновления через метод getUpdates и обрабатывает входящие сообщения
 * в {@link #onUpdateReceived(Update)}. На любое текстовое сообщение отвечает простым эхо
 * ("Вы написали: ...").
 * <p>
 * Конфигурационные свойства (инжектируются Spring):
 * <ul>
 *   <li><b>telegram.bot.username</b> — публичное имя бота (например, @my_bot)</li>
 *   <li><b>telegram.bot.token</b> — токен бота из BotFather</li>
 * </ul>
 * Регистрация бота выполняется при старте приложения в классе
 * {@code org.example.CoreCarService.Сonfig.TelegramWebhookConfig#registerTelegramLongPolling}.
 */
@Component
public class TelegramBot extends TelegramLongPollingBot {
    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    private static final String BTN_REGISTER = "Регистрация";
    private static final String BTN_LOGIN = "Авторизация";
    private static final String BTN_ABOUT_ME = "Обо мне";
    private static final String BTN_REPAIR_REQUEST = "Заявка на ремонт";
    private static final String BTN_MY_ORDERS = "Мои заказ наряды";

    private final RegistrationRepository registrationRepository;
    private final PasswordEncoder passwordEncoder;
    private final CarClientRepository carClientRepository;

    // Состояния пошаговой регистрации
    private enum RegState {
        NONE,
        AWAIT_FULLNAME,
        AWAIT_PHONE,
        AWAIT_EMAIL,
        AWAIT_LOGIN,
        AWAIT_PASSWORD,
        AWAIT_PASSWORD_CONFIRM,
        AWAIT_METHOD,
        AWAIT_GENDER,
        AWAIT_ROLE
    }

    // Простая сессия для накопления вводимых данных
    private static class RegistrationSession {
        String fullName;
        String phone;
        String email;
        String login;
        String password;
        String method;
        String gender;
        String role;
    }

    // Храним состояния и сессии по chatId
    private final Map<Long, RegState> regStates = new ConcurrentHashMap<>();
    private final Map<Long, RegistrationSession> regSessions = new ConcurrentHashMap<>();

    // Авторизация: состояния и сессии
    private enum AuthState { NONE, AWAIT_LOGIN, AWAIT_PASSWORD }
    private static class AuthSession { String login; }
    private final Map<Long, AuthState> authStates = new ConcurrentHashMap<>();
    private final Map<Long, AuthSession> authSessions = new ConcurrentHashMap<>();
    private final Map<Long, String> authenticatedLogins = new ConcurrentHashMap<>(); // chatId -> login

    // Редактирование полей "Обо мне"
    private enum EditField { NONE, FULLNAME, METHOD_COMMUNICATE, GENDER, EMAIL, PHONE }
    private final Map<Long, EditField> editStates = new ConcurrentHashMap<>();

    // Управление автомобилями: пошаговый процесс
    private enum CarFlow { NONE, ADD_MAKE, ADD_MODEL, ADD_YEAR, ADD_VIN, ADD_MILEAGE, DELETE_VIN }
    private static class CarSession {
        String make;
        String model;
        Integer year;
        String vin;
        Integer mileage;
    }
    private final Map<Long, CarFlow> carFlows = new ConcurrentHashMap<>();
    private final Map<Long, CarSession> carSessions = new ConcurrentHashMap<>();

    @Autowired
    public TelegramBot(RegistrationRepository registrationRepository,
                       PasswordEncoder passwordEncoder,
                       CarClientRepository carClientRepository) {
        this.registrationRepository = registrationRepository;
        this.passwordEncoder = passwordEncoder;
        this.carClientRepository = carClientRepository;
    }

    @Override
    /**
     * Обработка входящих обновлений от Telegram.
     * <p>
     * Если обновление содержит текстовое сообщение — отправляет ответ в виде простого эхо:
     * «Вы написали: {текст}».
     *
     * @param update объект обновления (сообщения, callback-и и т.п.)
     */
    public void onUpdateReceived(Update update) {
        SendMessage msg = null;

        // Обработка callback-ов от inline-кнопок
        if (update.hasCallbackQuery()) {
            CallbackQuery cq = update.getCallbackQuery();
            long chatId = cq.getMessage().getChatId();
            String data = cq.getData();

            if (!authenticatedLogins.containsKey(chatId)) {
                msg = new SendMessage(String.valueOf(chatId), "Сначала авторизуйтесь: нажмите 'Авторизация'.");
                msg.setReplyMarkup(mainKeyboard());
                safeExecute(msg); return;
            }

            switch (data) {
                case "EDIT_FULLNAME" -> {
                    editStates.put(chatId, EditField.FULLNAME);
                    msg = new SendMessage(String.valueOf(chatId), "Введите новое ФИО:");
                }
                case "EDIT_METHOD" -> {
                    editStates.put(chatId, EditField.METHOD_COMMUNICATE);
                    msg = new SendMessage(String.valueOf(chatId), "Введите способ связи (TELEGRAM/PHONE/EMAIL):");
                }
                case "EDIT_GENDER" -> {
                    editStates.put(chatId, EditField.GENDER);
                    msg = new SendMessage(String.valueOf(chatId), "Введите пол (MALE/FEMALE/OTHER):");
                }
                case "EDIT_EMAIL" -> {
                    editStates.put(chatId, EditField.EMAIL);
                    msg = new SendMessage(String.valueOf(chatId), "Введите новый email:");
                }
                case "EDIT_PHONE" -> {
                    editStates.put(chatId, EditField.PHONE);
                    msg = new SendMessage(String.valueOf(chatId), "Введите новый номер телефона в формате +7XXXXXXXXXX (можно ввести 8XXXXXXXXXX — преобразуем):");
                }
                case "ADD_CAR" -> {
                    carFlows.put(chatId, CarFlow.ADD_MAKE);
                    carSessions.put(chatId, new CarSession());
                    msg = new SendMessage(String.valueOf(chatId), "Добавление авто — Шаг 1/5: Марка (make):");
                }
                case "DELETE_CAR" -> {
                    carFlows.put(chatId, CarFlow.DELETE_VIN);
                    msg = new SendMessage(String.valueOf(chatId), "Удаление авто: введите VIN (17 символов):");
                }
                default -> {
                    msg = new SendMessage(String.valueOf(chatId), "Неизвестная команда.");
                }
            }
            safeExecute(msg); return;
        }

        // Обработка входящих сообщений (Long Polling)
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            // Приоритетная обработка основных команд-кнопок, чтобы они работали даже во время активных сессий
            if ("/start".equalsIgnoreCase(messageText)) {
                if (authenticatedLogins.containsKey(chatId)) {
                    String login = authenticatedLogins.get(chatId);
                    Optional<RegUserInfoEntity> opt = registrationRepository.findByLogin(login);
                    if (opt.isPresent()) {
                        SendMessage m = new SendMessage(String.valueOf(chatId), buildAboutMeText(opt.get()));
                        m.setReplyMarkup(aboutMeInlineKeyboard());
                        safeExecute(m); return;
                    } else {
                        SendMessage m = new SendMessage(String.valueOf(chatId), "Вы уже авторизованы.");
                        m.setReplyMarkup(authKeyboard());
                        safeExecute(m); return;
                    }
                } else {
                    SendMessage m = new SendMessage(String.valueOf(chatId), "Привет! Я бот автосервиса. Выберите действие:");
                    m.setReplyMarkup(mainKeyboard());
                    safeExecute(m); return;
                }
            } else if (BTN_REGISTER.equalsIgnoreCase(messageText.trim())) {
                regStates.put(chatId, RegState.AWAIT_FULLNAME);
                regSessions.remove(chatId);
                authStates.remove(chatId); authSessions.remove(chatId);
                safeExecute(new SendMessage(String.valueOf(chatId), "Шаг 1/8 — Введите ФИО:")); return;
            } else if (BTN_LOGIN.equalsIgnoreCase(messageText.trim())) {
                if (authenticatedLogins.containsKey(chatId)) {
                    String login = authenticatedLogins.get(chatId);
                    SendMessage m = new SendMessage(String.valueOf(chatId), "Вы уже авторизованы как: " + login);
                    m.setReplyMarkup(authKeyboard());
                    safeExecute(m); return;
                } else {
                    authStates.put(chatId, AuthState.AWAIT_LOGIN);
                    authSessions.remove(chatId);
                    regStates.remove(chatId); regSessions.remove(chatId);
                    safeExecute(new SendMessage(String.valueOf(chatId), "Введите логин:")); return;
                }
            } else if ("/logout".equalsIgnoreCase(messageText)) {
                authenticatedLogins.remove(chatId);
                SendMessage m = new SendMessage(String.valueOf(chatId), "Вы вышли из системы.");
                m.setReplyMarkup(mainKeyboard());
                safeExecute(m); return;
            } else if (BTN_ABOUT_ME.equalsIgnoreCase(messageText.trim())) {
                if (!authenticatedLogins.containsKey(chatId)) {
                    SendMessage m = new SendMessage(String.valueOf(chatId), "Сначала авторизуйтесь: нажмите 'Авторизация'.");
                    m.setReplyMarkup(mainKeyboard());
                    safeExecute(m); return;
                } else {
                    String login = authenticatedLogins.get(chatId);
                    Optional<RegUserInfoEntity> opt = registrationRepository.findByLogin(login);
                    if (opt.isPresent()) {
                        RegUserInfoEntity u = opt.get();
                        SendMessage m = new SendMessage(String.valueOf(chatId), buildAboutMeText(u));
                        m.setReplyMarkup(aboutMeInlineKeyboard());
                        safeExecute(m); return;
                    } else {
                        SendMessage m = new SendMessage(String.valueOf(chatId), "Профиль не найден. Попробуйте войти заново.");
                        m.setReplyMarkup(mainKeyboard());
                        safeExecute(m); return;
                    }
                }
            }

            // Если пользователь в режиме редактирования поля "Обо мне"
            EditField ef = editStates.getOrDefault(chatId, EditField.NONE);
            if (ef != EditField.NONE && !messageText.startsWith("/")) {
                String login = authenticatedLogins.get(chatId);
                if (login == null) {
                    editStates.remove(chatId);
                    SendMessage error = new SendMessage(String.valueOf(chatId), "Сессия истекла. Авторизуйтесь снова.");
                    error.setReplyMarkup(mainKeyboard());
                    safeExecute(error); return;
                }
                Optional<RegUserInfoEntity> opt = registrationRepository.findByLogin(login);
                if (opt.isEmpty()) {
                    editStates.remove(chatId);
                    SendMessage error = new SendMessage(String.valueOf(chatId), "Профиль не найден. Попробуйте войти заново.");
                    error.setReplyMarkup(mainKeyboard());
                    safeExecute(error); return;
                }

                RegUserInfoEntity u = opt.get();
                String val = messageText.trim();
                switch (ef) {
                    case FULLNAME -> {
                        if (val.isEmpty()) {
                            safeExecute(new SendMessage(String.valueOf(chatId), "ФИО не может быть пустым. Введите снова:")); return;
                        }
                        u.setFullName(val);
                    }
                    case METHOD_COMMUNICATE -> {
                        String up = val.toUpperCase();
                        if (!("TELEGRAM".equals(up) || "PHONE".equals(up) || "EMAIL".equals(up))) {
                            safeExecute(new SendMessage(String.valueOf(chatId), "Недопустимое значение. Введите TELEGRAM, PHONE или EMAIL:")); return;
                        }
                        u.setMethodCommunicate(up);
                    }
                    case GENDER -> {
                        String up = val.toUpperCase();
                        if (!("MALE".equals(up) || "FEMALE".equals(up) || "OTHER".equals(up))) {
                            safeExecute(new SendMessage(String.valueOf(chatId), "Недопустимое значение. Введите MALE, FEMALE или OTHER:")); return;
                        }
                        u.setGender(up);
                    }
                    case EMAIL -> {
                        String email = val;
                        // Простой форматный чек
                        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$") ) {
                            safeExecute(new SendMessage(String.valueOf(chatId), "Некорректный email. Введите снова:")); return;
                        }
                        // Проверка уникальности (разрешаем собственный текущий email)
                        boolean exists = registrationRepository.existsByEmail(email);
                        if (exists && (u.getEmail() == null || !u.getEmail().equalsIgnoreCase(email))) {
                            safeExecute(new SendMessage(String.valueOf(chatId), "Этот email уже используется. Введите другой:")); return;
                        }
                        u.setEmail(email);
                    }
                    case PHONE -> {
                        // Нормализация к формату +7XXXXXXXXXX
                        String digits = val.replaceAll("[^0-9]", "");
                        if (digits.length() == 11 && digits.startsWith("8")) {
                            digits = "7" + digits.substring(1);
                        }
                        if (!(digits.length() == 11 && digits.startsWith("7"))) {
                            safeExecute(new SendMessage(String.valueOf(chatId), "Некорректный номер. Требуется формат +7XXXXXXXXXX (11 цифр). Попробуйте снова:")); return;
                        }
                        String normalized = "+" + digits;
                        boolean exists = registrationRepository.existsByPhoneNumber(normalized);
                        if (exists && (u.getPhoneNumber() == null || !u.getPhoneNumber().equals(normalized))) {
                            safeExecute(new SendMessage(String.valueOf(chatId), "Этот номер уже используется. Введите другой:")); return;
                        }
                        u.setPhoneNumber(normalized);
                    }
                    default -> {}
                }

                registrationRepository.save(u);
                editStates.remove(chatId);

                // Показать обновлённый профиль с inline-кнопками
                SendMessage ok = new SendMessage(String.valueOf(chatId), buildAboutMeText(u));
                ok.setReplyMarkup(aboutMeInlineKeyboard());
                safeExecute(ok); return;
            }

            // Если пользователь в процессе управления авто (добавление/удаление)
            CarFlow cf = carFlows.getOrDefault(chatId, CarFlow.NONE);
            if (cf != CarFlow.NONE && !messageText.startsWith("/")) {
                String login = authenticatedLogins.get(chatId);
                if (login == null) {
                    carFlows.remove(chatId); carSessions.remove(chatId);
                    SendMessage error = new SendMessage(String.valueOf(chatId), "Сессия истекла. Авторизуйтесь снова.");
                    error.setReplyMarkup(mainKeyboard());
                    safeExecute(error); return;
                }
                Optional<RegUserInfoEntity> opt = registrationRepository.findByLogin(login);
                if (opt.isEmpty()) {
                    carFlows.remove(chatId); carSessions.remove(chatId);
                    SendMessage error = new SendMessage(String.valueOf(chatId), "Профиль не найден. Попробуйте войти заново.");
                    error.setReplyMarkup(mainKeyboard());
                    safeExecute(error); return;
                }
                RegUserInfoEntity user = opt.get();
                CarSession cs = carSessions.computeIfAbsent(chatId, k -> new CarSession());
                String val = messageText.trim();

                switch (cf) {
                    case ADD_MAKE -> {
                        if (val.isEmpty()) { safeExecute(new SendMessage(String.valueOf(chatId), "Марка не может быть пустой. Введите марку:")); return; }
                        cs.make = val; carFlows.put(chatId, CarFlow.ADD_MODEL);
                        safeExecute(new SendMessage(String.valueOf(chatId), "Шаг 2/5: Модель (model):")); return;
                    }
                    case ADD_MODEL -> {
                        if (val.isEmpty()) { safeExecute(new SendMessage(String.valueOf(chatId), "Модель не может быть пустой. Введите модель:")); return; }
                        cs.model = val; carFlows.put(chatId, CarFlow.ADD_YEAR);
                        safeExecute(new SendMessage(String.valueOf(chatId), "Шаг 3/5: Год выпуска (например, 2018):")); return;
                    }
                    case ADD_YEAR -> {
                        try {
                            int year = Integer.parseInt(val);
                            int current = java.time.Year.now().getValue();
                            if (year < 1950 || year > current + 1) {
                                safeExecute(new SendMessage(String.valueOf(chatId), "Некорректный год. Введите значение от 1950 до " + (current + 1) + ":")); return;
                            }
                            cs.year = year; carFlows.put(chatId, CarFlow.ADD_VIN);
                            safeExecute(new SendMessage(String.valueOf(chatId), "Шаг 4/5: VIN (17 символов, латиница и цифры):")); return;
                        } catch (NumberFormatException e) {
                            safeExecute(new SendMessage(String.valueOf(chatId), "Некорректный год. Введите число, например 2018:")); return;
                        }
                    }
                    case ADD_VIN -> {
                        String vin = val.toUpperCase();
                        if (!vin.matches("^[A-HJ-NPR-Z0-9]{17}$")) {
                            safeExecute(new SendMessage(String.valueOf(chatId), "Некорректный VIN. Введите 17 символов (без I,O,Q):")); return;
                        }
                        if (carClientRepository.existsByVin(vin)) {
                            safeExecute(new SendMessage(String.valueOf(chatId), "Такой VIN уже зарегистрирован. Введите другой VIN:")); return;
                        }
                        cs.vin = vin; carFlows.put(chatId, CarFlow.ADD_MILEAGE);
                        safeExecute(new SendMessage(String.valueOf(chatId), "Шаг 5/5: Пробег (км, целое число):")); return;
                    }
                    case ADD_MILEAGE -> {
                        try {
                            int mileage = Integer.parseInt(val);
                            if (mileage < 0 || mileage > 2_000_000) {
                                safeExecute(new SendMessage(String.valueOf(chatId), "Некорректный пробег. Введите число от 0 до 2000000:")); return;
                            }
                            cs.mileage = mileage;
                        } catch (NumberFormatException e) {
                            safeExecute(new SendMessage(String.valueOf(chatId), "Некорректный пробег. Введите целое число:")); return;
                        }

                        try {
                            CarClientEntity car = CarClientEntity.builder()
                                    .user(user)
                                    .make(cs.make)
                                    .model(cs.model)
                                    .carYear(cs.year)
                                    .vin(cs.vin)
                                    .mileage(cs.mileage)
                                    .build();
                            carClientRepository.save(car);
                        } catch (Exception ex) {
                            carFlows.remove(chatId); carSessions.remove(chatId);
                            safeExecute(new SendMessage(String.valueOf(chatId), "Ошибка сохранения авто. Попробуйте позже.")); return;
                        }

                        carFlows.remove(chatId); carSessions.remove(chatId);
                        SendMessage ok = new SendMessage(String.valueOf(chatId), "Автомобиль добавлен.\n\n" + buildAboutMeText(user));
                        ok.setReplyMarkup(aboutMeInlineKeyboard());
                        safeExecute(ok); return;
                    }
                    case DELETE_VIN -> {
                        String vin = val.toUpperCase();
                        if (!vin.matches("^[A-HJ-NPR-Z0-9]{17}$")) {
                            safeExecute(new SendMessage(String.valueOf(chatId), "Некорректный VIN. Введите 17 символов (без I,O,Q):")); return;
                        }
                        var carOpt = carClientRepository.findByVin(vin);
                        if (carOpt.isEmpty() || carOpt.get().getUser() == null || !carOpt.get().getUser().getId().equals(user.getId())) {
                            safeExecute(new SendMessage(String.valueOf(chatId), "Авто с таким VIN у вас не найдено. Проверьте VIN и попробуйте снова:")); return;
                        }
                        carClientRepository.delete(carOpt.get());
                        carFlows.remove(chatId); carSessions.remove(chatId);
                        SendMessage ok = new SendMessage(String.valueOf(chatId), "Автомобиль удалён.\n\n" + buildAboutMeText(user));
                        ok.setReplyMarkup(aboutMeInlineKeyboard());
                        safeExecute(ok); return;
                    }
                    default -> {}
                }
            }

            if ("/start".equalsIgnoreCase(messageText)) {
                if (authenticatedLogins.containsKey(chatId)) {
                    String login = authenticatedLogins.get(chatId);
                    Optional<RegUserInfoEntity> opt = registrationRepository.findByLogin(login);
                    if (opt.isPresent()) {
                        msg = new SendMessage(String.valueOf(chatId), buildAboutMeText(opt.get()));
                        msg.setReplyMarkup(aboutMeInlineKeyboard());
                    } else {
                        msg = new SendMessage(String.valueOf(chatId), "Вы уже авторизованы.");
                        msg.setReplyMarkup(authKeyboard());
                    }
                } else {
                    msg = new SendMessage(String.valueOf(chatId), "Привет! Я бот автосервиса. Выберите действие:");
                    msg.setReplyMarkup(mainKeyboard());
                }
            } else if (BTN_REGISTER.equalsIgnoreCase(messageText.trim())) {
                // Старт регистрации
                regStates.put(chatId, RegState.AWAIT_FULLNAME);
                regSessions.remove(chatId); // начнём заново
                authStates.remove(chatId); authSessions.remove(chatId); // сброс авторизации на всякий случай
                msg = new SendMessage(String.valueOf(chatId), "Шаг 1/8 — Введите ФИО:");
            } else if (BTN_LOGIN.equalsIgnoreCase(messageText.trim())) {
                // Старт авторизации
                if (authenticatedLogins.containsKey(chatId)) {
                    String login = authenticatedLogins.get(chatId);
                    msg = new SendMessage(String.valueOf(chatId), "Вы уже авторизованы как: " + login);
                    msg.setReplyMarkup(authKeyboard());
                } else {
                    authStates.put(chatId, AuthState.AWAIT_LOGIN);
                    authSessions.remove(chatId); // начнём заново
                    regStates.remove(chatId); regSessions.remove(chatId); // сброс регистрации на всякий случай
                    msg = new SendMessage(String.valueOf(chatId), "Введите логин:");
                }
            } else if ("/logout".equalsIgnoreCase(messageText)) {
                authenticatedLogins.remove(chatId);
                msg = new SendMessage(String.valueOf(chatId), "Вы вышли из системы.");
                msg.setReplyMarkup(mainKeyboard());
            } else if (BTN_ABOUT_ME.equalsIgnoreCase(messageText.trim())) {
                if (!authenticatedLogins.containsKey(chatId)) {
                    msg = new SendMessage(String.valueOf(chatId), "Сначала авторизуйтесь: нажмите 'Авторизация'.");
                    msg.setReplyMarkup(mainKeyboard());
                } else {
                    String login = authenticatedLogins.get(chatId);
                    Optional<RegUserInfoEntity> opt = registrationRepository.findByLogin(login);
                    if (opt.isPresent()) {
                        RegUserInfoEntity u = opt.get();
                        msg = new SendMessage(String.valueOf(chatId), buildAboutMeText(u));
                        msg.setReplyMarkup(aboutMeInlineKeyboard());
                    } else {
                        msg = new SendMessage(String.valueOf(chatId), "Профиль не найден. Попробуйте войти заново.");
                        msg.setReplyMarkup(mainKeyboard());
                    }
                }
            } else {
                // Если пользователь в процессе регистрации — обрабатываем шаг
                RegState state = regStates.getOrDefault(chatId, RegState.NONE);
                if (state != RegState.NONE) {
                    msg = handleRegistrationStep(chatId, state, messageText);
                } else if (authStates.getOrDefault(chatId, AuthState.NONE) != AuthState.NONE) {
                    msg = handleAuthStep(chatId, authStates.get(chatId), messageText);
                } else {
                    // По умолчанию — эхо и напоминание о доступных действиях
                    msg = new SendMessage(String.valueOf(chatId), "Вы написали: " + messageText + "\n\nДоступные действия: Регистрация, Авторизация");
                    msg.setReplyMarkup(mainKeyboard());
                }
            }

            safeExecute(msg);
        }
    }

    private void safeExecute(SendMessage msg) {
        if (msg == null) return;
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            // TODO: add logging
        }
    }

    /**
     * Проверка сложности пароля: минимум 6 символов, хотя бы одна буква и одна цифра.
     */
    private boolean isStrongPassword(String pwd) {
        if (pwd == null || pwd.length() < 6) return false;
        boolean hasLetter = false, hasDigit = false;
        for (char c : pwd.toCharArray()) {
            if (Character.isLetter(c)) hasLetter = true;
            if (Character.isDigit(c)) hasDigit = true;
            if (hasLetter && hasDigit) return true;
        }
        return false;
    }

    /**
     * Логика авторизации пользователя: логин -> пароль -> проверка в БД.
     */
    private SendMessage handleAuthStep(long chatId, AuthState state, String input) {
        String chat = String.valueOf(chatId);
        AuthSession s = authSessions.computeIfAbsent(chatId, k -> new AuthSession());

        switch (state) {
            case AWAIT_LOGIN -> {
                String login = input.trim();
                Optional<RegUserInfoEntity> opt = registrationRepository.findByLogin(login);
                if (opt.isEmpty()) {
                    return new SendMessage(chat, "Пользователь с таким логином не найден. Введите другой логин:");
                }
                s.login = login;
                authStates.put(chatId, AuthState.AWAIT_PASSWORD);
                return new SendMessage(chat, "Введите пароль:");
            }
            case AWAIT_PASSWORD -> {
                Optional<RegUserInfoEntity> opt = registrationRepository.findByLogin(s.login);
                if (opt.isEmpty()) {
                    authStates.put(chatId, AuthState.AWAIT_LOGIN);
                    return new SendMessage(chat, "Сессия устарела. Введите логин заново:");
                }
                RegUserInfoEntity user = opt.get();
                String provided = input;
                if (!passwordEncoder.matches(provided, user.getPassword())) {
                    return new SendMessage(chat, "Неверный пароль. Попробуйте ещё раз ввести пароль:");
                }
                authenticatedLogins.put(chatId, user.getLogin());
                authStates.remove(chatId);
                authSessions.remove(chatId);
                SendMessage ok = new SendMessage(chat, "Успешная авторизация! Вы вошли как: " + user.getLogin());
                ok.setReplyMarkup(authKeyboard());
                return ok;
            }
            default -> {
                authStates.remove(chatId);
                authSessions.remove(chatId);
                return new SendMessage(chat, "Состояние авторизации сброшено. Нажмите 'Авторизация' чтобы начать заново.");
            }
        }
    }

    /**
     * Логика пошагового мастера регистрации клиента и его авто.
     */
    private SendMessage handleRegistrationStep(long chatId, RegState state, String input) {
        RegistrationSession s = regSessions.computeIfAbsent(chatId, k -> new RegistrationSession());
        String chat = String.valueOf(chatId);

        switch (state) {
            case AWAIT_FULLNAME -> {
                if (input.length() < 3) {
                    return new SendMessage(chat, "ФИО слишком короткое. Повторите ввод:");
                }
                s.fullName = input.trim();
                regStates.put(chatId, RegState.AWAIT_PHONE);
                return new SendMessage(chat, "Шаг 2/8 — Введите номер телефона (в любом удобном формате):");
            }
            case AWAIT_PHONE -> {
                String digits = input.replaceAll("[^0-9]", "");
                if (digits.length() == 11 && digits.startsWith("8")) {
                    digits = "7" + digits.substring(1);
                }
                if (!(digits.length() == 11 && digits.startsWith("7"))) {
                    return new SendMessage(chat, "Некорректный номер. Требуется формат +7XXXXXXXXXX (11 цифр). Введите снова:");
                }
                String normalized = "+" + digits;
                if (registrationRepository.existsByPhoneNumber(normalized)) {
                    return new SendMessage(chat, "Этот телефон уже зарегистрирован. Введите другой номер:");
                }
                s.phone = normalized;
                regStates.put(chatId, RegState.AWAIT_EMAIL);
                return new SendMessage(chat, "Шаг 3/8 — Введите email:");
            }
            case AWAIT_EMAIL -> {
                if (registrationRepository.existsByEmail(input.trim())) {
                    return new SendMessage(chat, "Этот email уже зарегистрирован. Введите другой email:");
                }
                s.email = input.trim();
                regStates.put(chatId, RegState.AWAIT_LOGIN);
                return new SendMessage(chat, "Шаг 4/8 — Придумайте логин:");
            }
            case AWAIT_LOGIN -> {
                if (registrationRepository.existsByLogin(input.trim())) {
                    return new SendMessage(chat, "Логин занят. Введите другой логин:");
                }
                s.login = input.trim();
                regStates.put(chatId, RegState.AWAIT_PASSWORD);
                return new SendMessage(chat, "Шаг 5/8 — Введите пароль (мин. 6 символов, буквы и цифры):");
            }
            case AWAIT_PASSWORD -> {
                String pwd = input;
                if (!isStrongPassword(pwd)) {
                    return new SendMessage(chat, "Слабый пароль. Требуется минимум 6 символов, хотя бы одна буква и одна цифра. Введите пароль ещё раз:");
                }
                s.password = pwd;
                regStates.put(chatId, RegState.AWAIT_PASSWORD_CONFIRM);
                return new SendMessage(chat, "Подтвердите пароль, введя его ещё раз:");
            }
            case AWAIT_PASSWORD_CONFIRM -> {
                if (!input.equals(s.password)) {
                    regStates.put(chatId, RegState.AWAIT_PASSWORD);
                    return new SendMessage(chat, "Пароли не совпадают. Введите пароль заново:");
                }
                regStates.put(chatId, RegState.AWAIT_METHOD);
                return new SendMessage(chat, "Шаг 6/8 — Предпочитаемый способ связи (TELEGRAM/PHONE/EMAIL). По умолчанию TELEGRAM:");
            }
            case AWAIT_METHOD -> {
                String val = input.trim().toUpperCase();
                if (val.isEmpty()) val = "TELEGRAM";
                if (!List.of("TELEGRAM","PHONE","EMAIL").contains(val)) {
                    return new SendMessage(chat, "Недопустимое значение. Введите TELEGRAM, PHONE или EMAIL:");
                }
                s.method = val;
                regStates.put(chatId, RegState.AWAIT_GENDER);
                return new SendMessage(chat, "Шаг 7/8 — Пол (MALE/FEMALE/OTHER). Можно пропустить, отправив '-' :");
            }
            case AWAIT_GENDER -> {
                String val = input.trim();
                if (!val.equals("-")) {
                    String up = val.toUpperCase();
                    if (!List.of("MALE","FEMALE","OTHER").contains(up)) {
                        return new SendMessage(chat, "Недопустимое значение. Введите MALE, FEMALE, OTHER или '-' чтобы пропустить:");
                    }
                    s.gender = up;
                } else {
                    s.gender = null;
                }
                regStates.put(chatId, RegState.AWAIT_ROLE);
                return new SendMessage(chat, "Шаг 8/8 — Роль (USER/ADMIN). По умолчанию USER:");
            }
            case AWAIT_ROLE -> {
                String val = input.trim().toUpperCase();
                if (val.isEmpty()) val = "USER";
                if (!List.of("USER","ADMIN").contains(val)) {
                    return new SendMessage(chat, "Недопустимое значение. Введите USER или ADMIN:");
                }
                s.role = val;

                // Сохранение пользователя
                try {
                    RegUserInfoEntity user = RegUserInfoEntity.builder()
                            .login(s.login)
                            .fullName(s.fullName)
                            .email(s.email)
                            .password(passwordEncoder.encode(s.password))
                            .phoneNumber(s.phone)
                            .userType("CLIENT")
                            .registrationUserStatus("ACTIVE")
                            .methodCommunicate(s.method)
                            .gender(s.gender)
                            .createdAt(LocalDateTime.now())
                            .role(s.role)
                            .build();
                    registrationRepository.save(user);
                } catch (Exception e) {
                    regStates.remove(chatId);
                    regSessions.remove(chatId);
                    return new SendMessage(chat, "Произошла ошибка при сохранении регистрации. Попробуйте позже.");
                }

                regStates.remove(chatId);
                regSessions.remove(chatId);
                SendMessage ok = new SendMessage(chat, "Регистрация успешно завершена! Ваш профиль сохранён.");
                ok.setReplyMarkup(mainKeyboard());
                return ok;
            }
            default -> {
                regStates.remove(chatId);
                regSessions.remove(chatId);
                return new SendMessage(chat, "Состояние регистрации сброшено. Нажмите 'Регистрация' чтобы начать заново.");
            }
        }
    }

    /**
     * Основная клавиатура с кнопками «Регистрация» и «Авторизация».
     */
    private ReplyKeyboardMarkup mainKeyboard() {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);

        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton(BTN_REGISTER));
        row1.add(new KeyboardButton(BTN_LOGIN));
        rows.add(row1);

        keyboard.setKeyboard(rows);
        return keyboard;
    }

    /** Текст раздела "Обо мне" для пользователя, включая список автомобилей */
    private String buildAboutMeText(RegUserInfoEntity u) {
        String createdAt = (u.getCreatedAt() == null) ? "-" : u.getCreatedAt().toString();
        StringBuilder sb = new StringBuilder();
        sb.append("Ваш профиль:\n")
          .append("Логин (нельзя изменить): ").append(nullSafe(u.getLogin())).append("\n")
          .append("ФИО: ").append(nullSafe(u.getFullName())).append("\n")
          .append("Email: ").append(nullSafe(u.getEmail())).append("\n")
          .append("Телефон: ").append(nullSafe(u.getPhoneNumber())).append("\n")
          .append("Тип пользователя (нельзя изменить): ").append(nullSafe(u.getUserType())).append("\n")
          .append("Статус регистрации: ").append(nullSafe(u.getRegistrationUserStatus())).append("\n")
          .append("Способ связи: ").append(nullSafe(u.getMethodCommunicate())).append("\n")
          .append("Пол: ").append(nullSafe(u.getGender())).append("\n")
          .append("Создан: ").append(createdAt).append("\n")
          .append("Роль (нельзя изменить): ").append(nullSafe(u.getRole()));

        if (u.getId() != null) {
            List<CarClientEntity> cars = carClientRepository.findAllByUser_Id(u.getId());
            if (cars != null && !cars.isEmpty()) {
                sb.append("\n\nМои автомобили:");
                int idx = 1;
                for (CarClientEntity c : cars) {
                    sb.append("\n").append(idx++).append(") ")
                      .append(nullSafe(c.getMake())).append(" ")
                      .append(nullSafe(c.getModel())).append(", ")
                      .append(c.getCarYear() == null ? "-" : c.getCarYear()).append(" г.")
                      .append("\n    VIN: ").append(nullSafe(c.getVin()))
                      .append("\n    Пробег: ").append(c.getMileage() == null ? "-" : c.getMileage() + " км");
                }
            } else {
                sb.append("\n\nМои автомобили: нет записей");
            }
        }
        return sb.toString();
    }

    /** Inline-кнопки для редактирования полей профиля */
    private InlineKeyboardMarkup aboutMeInlineKeyboard() {
        InlineKeyboardButton editFullName = new InlineKeyboardButton("Изменить ФИО");
        editFullName.setCallbackData("EDIT_FULLNAME");

        InlineKeyboardButton editMethod = new InlineKeyboardButton("Изменить способ связи");
        editMethod.setCallbackData("EDIT_METHOD");

        InlineKeyboardButton editGender = new InlineKeyboardButton("Изменить пол");
        editGender.setCallbackData("EDIT_GENDER");

        InlineKeyboardButton editEmail = new InlineKeyboardButton("Изменить email");
        editEmail.setCallbackData("EDIT_EMAIL");

        InlineKeyboardButton editPhone = new InlineKeyboardButton("Изменить телефон");
        editPhone.setCallbackData("EDIT_PHONE");

        InlineKeyboardButton addCar = new InlineKeyboardButton("Добавить авто");
        addCar.setCallbackData("ADD_CAR");

        InlineKeyboardButton deleteCar = new InlineKeyboardButton("Удалить авто");
        deleteCar.setCallbackData("DELETE_CAR");

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> r1 = new ArrayList<>(); r1.add(editFullName);
        List<InlineKeyboardButton> r2 = new ArrayList<>(); r2.add(editMethod);
        List<InlineKeyboardButton> r3 = new ArrayList<>(); r3.add(editGender);
        List<InlineKeyboardButton> r4 = new ArrayList<>(); r4.add(editEmail);
        List<InlineKeyboardButton> r5 = new ArrayList<>(); r5.add(editPhone);
        List<InlineKeyboardButton> r6 = new ArrayList<>(); r6.add(addCar);
        List<InlineKeyboardButton> r7 = new ArrayList<>(); r7.add(deleteCar);
        rows.add(r1); rows.add(r2); rows.add(r3); rows.add(r4); rows.add(r5); rows.add(r6); rows.add(r7);

        InlineKeyboardMarkup m = new InlineKeyboardMarkup();
        m.setKeyboard(rows);
        return m;
    }

    /**
     * Клавиатура для авторизованного пользователя.
     */
    private ReplyKeyboardMarkup authKeyboard() {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);

        List<KeyboardRow> rows = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton(BTN_ABOUT_ME));
        row1.add(new KeyboardButton(BTN_REPAIR_REQUEST));
        rows.add(row1);

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton(BTN_MY_ORDERS));
        rows.add(row2);

        KeyboardRow row3 = new KeyboardRow();
        row3.add(new KeyboardButton("/logout"));
        rows.add(row3);

        keyboard.setKeyboard(rows);
        return keyboard;
    }

    /** Простая защита от null при выводе строковых полей */
    private String nullSafe(String s) {
        return s == null ? "-" : s;
    }

    @Override
    /**
     * Возвращает публичное имя бота, зарегистрированное у BotFather.
     * Берётся из свойства {@code telegram.bot.username}.
     *
     * @return username бота (например, @my_bot)
     */
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    /**
     * Возвращает токен бота для авторизации в Telegram Bot API.
     * Берётся из свойства {@code telegram.bot.token}.
     *
     * @return секретный токен бота
     */
    public String getBotToken() {
        return botToken;
    }
}
