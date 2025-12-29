package com.example.expense.service;

import com.example.expense.model.User;
import com.example.expense.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // регистрация пользователя
    public User register(User user) {
        // Проверяем, существует ли пользователь
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("пользователь с таким именем уже существует");
        }

        // Сохраняем пользователя (пароль без шифрования)
        return userRepository.save(user);
    }

    // авторизация
    public User login(String username, String password) {
        return userRepository.findByUsernameAndPassword(username, password)
                .orElseThrow(() -> new RuntimeException("неверное имя пользователя или пароль"));
    }

    // получить пользователя по имени
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}