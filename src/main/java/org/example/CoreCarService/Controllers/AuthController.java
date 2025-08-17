package org.example.CoreCarService.Controllers;

import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.example.CoreCarService.DatabaseEntity.RegUserInfoEntity;
import org.example.CoreCarService.Repository.HistoryRepository;
import org.example.CoreCarService.Repository.RegistrationRepository;
import org.example.CoreCarService.Сonfig.JwtTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import org.example.CoreCarService.DatabaseEntity.UserActivityHistoryEntity;

record LoginRequest(@NotBlank String login, @NotBlank String password) {}

record LoginResponse(String token) {}

record RegisterRequest(
        @NotBlank
        @Size(min = 3, max = 30)
        @Pattern(regexp = "^[A-Za-z0-9._-]+$",
                message = "Имя пользователя может содержать буквы, цифры, точки, тире и подчеркивание.")
        String login,
        @NotBlank
        @Size(min = 8, max = 30)
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,}$",
                message = "Пароль может содержать буквы, цифры, точки, тире и подчеркивание.")
        String password,
        @NotBlank
        @Size(min = 3, max = 30)
        @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
                message = "Некорректный формат email")
        String email,
        @NotBlank
        @Size(min = 7, max = 15)
        @Pattern( regexp = "^\\+?[0-9.\\-()\\s]{7,15}$",
                message = "Некорректный формат телефона")
        String telephone,
        @NotBlank
        @Size(min = 1, max = 100)
        @Pattern(regexp = "^[А-ЯЁ][а-яё]+(-[А-ЯЁ][а-яё]+)?\\s[А-ЯЁ][а-яё]+\\s[А-ЯЁ][а-яё]+$",
                message = "Некорректное ФИО")
        String fullName,
        @NotBlank
        @Size(min = 1, max = 15)
        @Pattern(regexp = "^(Мужской|Женский)$",
                message = "Некорректный формат пола")
        String gender

)
{}

record RegisterResponse(String login, String fullName, String email, String phoneNumber, String gender, String role, String token) {}

@RestController
@RequestMapping("/auth")
@Tag(name = "Authorization", description = "Регистрация и авторизация пользователя")
public class AuthController {
        private final PasswordEncoder passwordEncoder;
        private final RegistrationRepository registrationRepository;
        private final HistoryRepository historyRepository;
        private final JwtTokenService jwtTokenService;

        public AuthController(PasswordEncoder passwordEncoder,
                              RegistrationRepository registrationRepository,
                              HistoryRepository historyRepository,
                              JwtTokenService jwtTokenService) {
                this.historyRepository = historyRepository;
                this.passwordEncoder = passwordEncoder;
                this.registrationRepository = registrationRepository;
                this.jwtTokenService = jwtTokenService;
        }

        @PostMapping("/login")
        @Operation(summary = "Вход пользователя", responses = {
                @ApiResponse(responseCode = "200", description = "Успешный вход",
                        content = @Content(schema = @Schema(implementation = LoginResponse.class))),
                @ApiResponse(responseCode = "401", description = "Неверные учетные данные")
        })
        /**
         * Аутентифицирует пользователя и возвращает JWT-токен при успешном входе.
         *
         * @param request логин и пароль
         * @return 200 OK с токеном или 401 при неверных учетных данных
         */
        public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
                return registrationRepository.findByLogin(request.login())
                        .filter(u -> passwordEncoder.matches(request.password(), u.getPassword()))
                        .<ResponseEntity<?>>map(u -> {
                                String token = jwtTokenService.generateToken(u.getLogin(), u.getRole());

                                // Лог успешной авторизации
                                UserActivityHistoryEntity success = new UserActivityHistoryEntity();
                                success.setUser(u);
                                success.setActivityAt(String.valueOf(LocalDateTime.now()));
                                historyRepository.save(success);

                                return ResponseEntity.ok(new LoginResponse(token));
                        })
                        .orElseGet(() -> {

                                return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
                        });
        }

        @PostMapping("/register")
        @Operation(summary = "Регистрация нового пользователя",
                description = "Требования: username 3..30 (латиница, цифры, .-_), пароль минимум 8 символов и соответствует политике сложности; fullName: строго три компонента — Фамилия Имя Отчество, каждый компонент минимум 2 символа",
                responses = {
                        @ApiResponse(responseCode = "200", description = "Пользователь зарегистрирован",
                                content = @Content(schema = @Schema(implementation = RegisterResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Ошибки валидации"),
                        @ApiResponse(responseCode = "409", description = "Имя пользователя занято")
                })
        /**
         * Регистрирует нового пользователя и возвращает JWT-токен.
         *
         * @param request данные регистрации (username, password, fullName)
         * @return 200 OK с данными созданного пользователя и токеном либо 409, если имя занято
         */
        public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
                if (registrationRepository.existsByLogin(request.login())) {
                        return ResponseEntity.status(409).body(Map.of("error", "Username already taken"));
                }
                if (registrationRepository.existsByEmail(request.email())) {
                        return ResponseEntity.status(409).body(Map.of("error", "Email already taken"));
                }
                if (registrationRepository.existsByPhoneNumber(request.telephone())) {
                        return ResponseEntity.status(409).body(Map.of("error", "Phone number already taken"));
                }

                RegUserInfoEntity user = new RegUserInfoEntity();
                user.setLogin(request.login());
                user.setPassword(passwordEncoder.encode(request.password()));
                user.setFullName(request.fullName());
                user.setEmail(request.email());
                user.setPhoneNumber(request.telephone());
                user.setGender(request.gender());
                user.setRole("user");
                user.setRegistrationUserStatus("registered");
                user.setCreatedAt(LocalDateTime.now());
                registrationRepository.save(user);

                return ResponseEntity.ok(new RegisterResponse(
                        user.getLogin(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getPhoneNumber(),
                        user.getGender(),
                        user.getRole(),
                        jwtTokenService.generateToken(user.getLogin(), user.getRole())));
        }


}
