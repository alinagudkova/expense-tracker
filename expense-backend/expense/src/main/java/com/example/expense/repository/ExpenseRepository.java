package com.example.expense.repository;

import com.example.expense.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    //фильтр по категории
    List<Expense> findByCategory(String category);

    //по дате
    List<Expense> findByDate(LocalDate date);

    // по периоду от и до
    List<Expense> findByDateBetween(LocalDate startDate, LocalDate endDate);

    //фильтр по категории и периоду
    List<Expense> findByCategoryAndDateBetween(String category, LocalDate startDate, LocalDate endDate);

    //получить все уникальные категории
    @Query("SELECT DISTINCT e.category FROM Expense e WHERE e.category IS NOT NULL")
    List<String> findDistinctCategories();

    //получить расходы за последние N дней
    @Query("SELECT e FROM Expense e WHERE e.date >= :startDate ORDER BY e.date DESC")
    List<Expense> findRecentExpenses(@Param("startDate") LocalDate startDate);

}