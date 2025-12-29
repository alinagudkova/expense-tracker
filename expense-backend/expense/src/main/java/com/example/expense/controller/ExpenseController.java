package com.example.expense.controller;

import com.example.expense.model.Expense;
import com.example.expense.service.ExpenseService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/expenses")
@CrossOrigin(origins = "http://localhost:5173")
public class ExpenseController {

    private final ExpenseService service;

    public ExpenseController(ExpenseService service) {
        this.service = service;
    }

    // получить все расходы
    @GetMapping
    public List<Expense> getAll() {
        return service.getAll();
    }

    // создать новый расход - ОСТАВЬТЕ ТОЛЬКО ЭТОТ МЕТОД!
    @PostMapping
    public Expense create(@RequestBody Expense expense) {
        return service.save(expense);
    }

    // удалить расход
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }

    // фильтры

    // по категории
    @GetMapping("/category/{category}")
    public List<Expense> getByCategory(@PathVariable String category) {
        return service.getByCategory(category);
    }

    // дата
    @GetMapping("/date/{date}")
    public List<Expense> getByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return service.getByDate(date);
    }

    // период
    @GetMapping("/period")
    public List<Expense> getByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return service.getByPeriod(startDate, endDate);
    }

    // универсальный фильтр
    @GetMapping("/filter")
    public List<Expense> filter(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return service.filterExpenses(category, startDate, endDate);
    }

    // аналитика по категориям
    @GetMapping("/analytics/category")
    public Map<String, Double> getAnalyticsByCategory() {
        return service.getAnalyticsByCategory();
    }

    // общая сумма всех расходов
    @GetMapping("/analytics/total")
    public Double getTotalAmount() {
        return service.getTotalAmount();
    }

    // все уникальные категории
    @GetMapping("/categories")
    public List<String> getAllCategories() {
        return service.getAllCategories();
    }

    // за последние 30 дней
    @GetMapping("/recent")
    public List<Expense> getRecentExpenses() {
        return service.getRecentExpenses();
    }
}