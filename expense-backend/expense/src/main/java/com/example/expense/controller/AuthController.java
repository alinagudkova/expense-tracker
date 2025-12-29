package com.example.expense.controller;

import com.example.expense.model.User;
import com.example.expense.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final UserService userService;

    // простое хранение в памяти
    private User currentUser = null;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // регистрация
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> userData) {
        try {
            String username = userData.get("username");
            String password = userData.get("password");

            if (username == null || username.trim().isEmpty()) {
                throw new RuntimeException("имя пользователя не может быть пустым");
            }

            if (password == null || password.trim().isEmpty()) {
                throw new RuntimeException("пароль не может быть пустым");
            }

            User user = new User();
            user.setUsername(username);
            user.setPassword(password);

            User registeredUser = userService.register(user);
            currentUser = registeredUser;

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "регистрация успешна");
            response.put("user", Map.of(
                    "id", registeredUser.getId(),
                    "username", registeredUser.getUsername()
            ));

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // вход
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        try {
            String username = credentials.get("username");
            String password = credentials.get("password");

            if (username == null || password == null) {
                throw new RuntimeException("заполните все поля");
            }

            User user = userService.login(username, password);
            currentUser = user;

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "вход выполнен успешно");
            response.put("user", Map.of(
                    "id", user.getId(),
                    "username", user.getUsername()
            ));

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // проверка авторизации
    @GetMapping("/check")
    public ResponseEntity<?> checkAuth() {
        if (currentUser != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("authenticated", true);
            response.put("user", Map.of(
                    "id", currentUser.getId(),
                    "username", currentUser.getUsername()
            ));
            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("authenticated", false);
            return ResponseEntity.ok(response);
        }
    }

    // выход
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        currentUser = null;
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "выход выполнен успешно");
        return ResponseEntity.ok(response);
    }

    // тестовый эндпоинт
    @GetMapping("/test")
    public String test() {
        return "Auth API работает! Текущий пользователь: " +
                (currentUser != null ? currentUser.getUsername() : "не авторизован");
    }
}