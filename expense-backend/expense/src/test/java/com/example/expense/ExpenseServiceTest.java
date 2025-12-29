package com.example.expense.service;

import com.example.expense.model.Expense;
import com.example.expense.repository.ExpenseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository repository;

    @InjectMocks
    private ExpenseService service;

    @Test
    void save_shouldSetTodayDateIfNull() {
        // Arrange
        Expense expense = new Expense("Юбка", 1500.0, null, "Покупки", null);
        when(repository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Expense saved = service.save(expense);

        // Assert
        assertNotNull(saved.getDate());
        assertEquals(LocalDate.now(), saved.getDate());
        verify(repository).save(expense);
    }

    @Test
    void getTotalAmount_shouldReturnCorrectSum() {
        // Arrange
        when(repository.findAll()).thenReturn(List.of(
                new Expense("A", 100.0, LocalDate.now(), "X", null),
                new Expense("B", 200.0, LocalDate.now(), "Y", null)
        ));

        // Act
        Double total = service.getTotalAmount();

        // Assert
        assertEquals(300.0, total);
        verify(repository).findAll();
    }

    @Test
    void filterExpenses_byCategory() {
        // Arrange
        String category = "Покупки";
        List<Expense> expected = List.of(new Expense());
        when(repository.findByCategory(category)).thenReturn(expected);

        // Act
        List<Expense> result = service.filterExpenses(category, null, null);

        // Assert
        assertEquals(1, result.size());
        assertEquals(expected, result);
        verify(repository).findByCategory(category);
    }

    @Test
    void filterExpenses_byPeriod() {
        // Arrange
        LocalDate start = LocalDate.now().minusDays(5);
        LocalDate end = LocalDate.now();
        when(repository.findByDateBetween(start, end)).thenReturn(List.of(new Expense()));

        // Act
        List<Expense> result = service.filterExpenses(null, start, end);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    void analyticsByCategory_shouldGroupCorrectly() {
        // Arrange
        when(repository.findAll()).thenReturn(List.of(
                new Expense("A", 100.0, LocalDate.now(), "Еда", null),
                new Expense("B", 200.0, LocalDate.now(), "Еда", null),
                new Expense("C", 50.0, LocalDate.now(), null, null)
        ));

        // Act
        Map<String, Double> analytics = service.getAnalyticsByCategory();

        // Assert
        assertEquals(2, analytics.size());
        assertEquals(300.0, analytics.get("Еда"));
        assertEquals(50.0, analytics.get("Без категории"));
    }

    @Test
    void getRecentExpenses_shouldCallRepository() {
        // Arrange
        when(repository.findRecentExpenses(any(LocalDate.class))).thenReturn(List.of(new Expense()));

        // Act
        List<Expense> result = service.getRecentExpenses();

        // Assert
        assertFalse(result.isEmpty());
        verify(repository).findRecentExpenses(any(LocalDate.class));
    }

    @Test
    void getByCategory_shouldReturnFilteredExpenses() {
        // Arrange
        String category = "Еда";
        List<Expense> expected = List.of(
                new Expense("Обед", 500.0, LocalDate.now(), "Еда", null)
        );
        when(repository.findByCategory(category)).thenReturn(expected);

        // Act
        List<Expense> result = service.getByCategory(category);

        // Assert
        assertEquals(1, result.size());
        assertEquals("Обед", result.get(0).getTitle());
        verify(repository).findByCategory(category);
    }

    @Test
    void getByDate_shouldReturnExpensesForSpecificDate() {
        // Arrange
        LocalDate date = LocalDate.of(2025, 12, 28);
        when(repository.findByDate(date)).thenReturn(List.of(new Expense()));

        // Act
        List<Expense> result = service.getByDate(date);

        // Assert
        assertFalse(result.isEmpty());
        verify(repository).findByDate(date);
    }

    @Test
    void getByPeriod_shouldReturnExpensesBetweenDates() {
        // Arrange
        LocalDate start = LocalDate.now().minusDays(7);
        LocalDate end = LocalDate.now();
        when(repository.findByDateBetween(start, end)).thenReturn(List.of(new Expense()));

        // Act
        List<Expense> result = service.getByPeriod(start, end);

        // Assert
        assertFalse(result.isEmpty());
        verify(repository).findByDateBetween(start, end);
    }

    @Test
    void getByCategoryAndPeriod_shouldReturnFilteredExpenses() {
        // Arrange
        String category = "Транспорт";
        LocalDate start = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now();
        when(repository.findByCategoryAndDateBetween(category, start, end))
                .thenReturn(List.of(new Expense()));

        // Act
        List<Expense> result = service.getByCategoryAndPeriod(category, start, end);

        // Assert
        assertFalse(result.isEmpty());
        verify(repository).findByCategoryAndDateBetween(category, start, end);
    }

    @Test
    void getAllCategories_shouldReturnUniqueCategories() {
        // Arrange
        when(repository.findDistinctCategories()).thenReturn(List.of("Еда", "Транспорт"));

        // Act
        List<String> categories = service.getAllCategories();

        // Assert
        assertEquals(2, categories.size());
        assertTrue(categories.contains("Еда"));
        verify(repository).findDistinctCategories();
    }

    @Test
    void getTotalByPeriod_shouldCalculateSumForPeriod() {
        // Arrange
        LocalDate start = LocalDate.now().minusDays(5);
        LocalDate end = LocalDate.now();
        List<Expense> expenses = List.of(
                new Expense("A", 100.0, start, "Food", null),
                new Expense("B", 200.0, end, "Food", null)
        );
        when(repository.findByDateBetween(start, end)).thenReturn(expenses);

        // Act
        Double total = service.getTotalByPeriod(start, end);

        // Assert
        assertEquals(300.0, total);
        verify(repository).findByDateBetween(start, end);
    }

    @Test
    void getAveragePerDay_withEmptyList_shouldReturnZero() {
        // Arrange
        when(repository.findAll()).thenReturn(List.of());

        // Act
        Double average = service.getAveragePerDay();

        // Assert
        assertEquals(0.0, average, 0.001);
    }

    @Test
    void getAveragePerDay_withSingleExpense_shouldReturnAmount() {
        // Arrange
        LocalDate date = LocalDate.of(2025, 12, 28);
        Expense expense = new Expense("Test", 100.0, date, "Test", null);
        when(repository.findAll()).thenReturn(List.of(expense));

        // Act
        Double average = service.getAveragePerDay();

        // Assert
        assertEquals(100.0, average, 0.001);
    }
}