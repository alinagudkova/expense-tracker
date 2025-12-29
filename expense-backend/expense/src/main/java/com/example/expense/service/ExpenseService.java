package com.example.expense.service;

import com.example.expense.model.Expense;
import com.example.expense.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExpenseService {
    private final ExpenseRepository repository;

    public ExpenseService(ExpenseRepository repository) {
        this.repository = repository;
    }

    public List<Expense> getAll() {
        return repository.findAll();
    }

    public Expense save(Expense expense) {
        // Если дата не указана, ставим сегодняшнюю
        if (expense.getDate() == null) {
            expense.setDate(LocalDate.now());
        }
        return repository.save(expense);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    // ========== ФИЛЬТРАЦИЯ ==========

    public List<Expense> getByCategory(String category) {
        return repository.findByCategory(category);
    }

    public List<Expense> getByDate(LocalDate date) {
        return repository.findByDate(date);
    }

    public List<Expense> getByPeriod(LocalDate startDate, LocalDate endDate) {
        return repository.findByDateBetween(startDate, endDate);
    }

    public List<Expense> getByCategoryAndPeriod(String category, LocalDate startDate, LocalDate endDate) {
        return repository.findByCategoryAndDateBetween(category, startDate, endDate);
    }

    // Универсальный метод фильтрации
    public List<Expense> filterExpenses(String category, LocalDate startDate, LocalDate endDate) {
        if (category != null && startDate != null && endDate != null) {
            return getByCategoryAndPeriod(category, startDate, endDate);
        } else if (category != null && startDate == null && endDate == null) {
            return getByCategory(category);
        } else if (category == null && startDate != null && endDate != null) {
            return getByPeriod(startDate, endDate);
        } else {
            return getAll();
        }
    }

    // ========== АНАЛИТИКА ==========

    // Сумма расходов по категориям
    public Map<String, Double> getAnalyticsByCategory() {
        List<Expense> allExpenses = getAll();
        Map<String, Double> analytics = new HashMap<>();

        for (Expense expense : allExpenses) {
            String category = expense.getCategory() != null ? expense.getCategory() : "Без категории";
            analytics.put(category, analytics.getOrDefault(category, 0.0) + expense.getAmount());
        }

        return analytics;
    }

    // Общая сумма всех расходов
    public Double getTotalAmount() {
        return getAll().stream()
                .mapToDouble(Expense::getAmount)
                .sum();
    }

    // Сумма за период
    public Double getTotalByPeriod(LocalDate startDate, LocalDate endDate) {
        return getByPeriod(startDate, endDate).stream()
                .mapToDouble(Expense::getAmount)
                .sum();
    }

    // Средний расход в день
    public Double getAveragePerDay() {
        List<Expense> allExpenses = getAll();
        if (allExpenses.isEmpty()) return 0.0;

        // Находим разницу между самой ранней и поздней датой
        Optional<LocalDate> minDate = allExpenses.stream()
                .map(Expense::getDate)
                .min(LocalDate::compareTo);
        Optional<LocalDate> maxDate = allExpenses.stream()
                .map(Expense::getDate)
                .max(LocalDate::compareTo);

        if (minDate.isPresent() && maxDate.isPresent()) {
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(minDate.get(), maxDate.get()) + 1;
            return getTotalAmount() / daysBetween;
        }

        return getTotalAmount();
    }

    // Получить все категории
    public List<String> getAllCategories() {
        return repository.findDistinctCategories();
    }

    // Получить расходы за последние 30 дней
    public List<Expense> getRecentExpenses() {
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        return repository.findRecentExpenses(thirtyDaysAgo);
    }
}