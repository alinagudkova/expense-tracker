package com.example.expense.controller;

import com.example.expense.model.Expense;
import com.example.expense.service.ExpenseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExpenseController.class)
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExpenseService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAll_shouldReturnList() throws Exception {
        // Arrange
        when(service.getAll()).thenReturn(List.of(new Expense()));

        // Act & Assert
        mockMvc.perform(get("/expenses"))
                .andExpect(status().isOk());
    }

    @Test
    void createExpense_shouldReturnSavedExpense() throws Exception {
        // Arrange
        Expense expense = new Expense("Юбка", 1500.0, LocalDate.now(), "Покупки", null);

        when(service.save(any(Expense.class))).thenReturn(expense);

        // Act & Assert
        mockMvc.perform(post("/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expense)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Юбка"))
                .andExpect(jsonPath("$.amount").value(1500.0));
    }

    @Test
    void deleteExpense_shouldReturnOk() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/expenses/1"))
                .andExpect(status().isOk());
    }

    @Test
    void analyticsByCategory_shouldReturnMap() throws Exception {
        // Arrange
        when(service.getAnalyticsByCategory()).thenReturn(Map.of("Еда", 300.0));

        // Act & Assert
        mockMvc.perform(get("/expenses/analytics/category"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Еда").value(300.0));
    }

    @Test
    void getRecentExpenses_shouldReturnList() throws Exception {
        // Arrange
        when(service.getRecentExpenses()).thenReturn(List.of(new Expense()));

        // Act & Assert
        mockMvc.perform(get("/expenses/recent"))
                .andExpect(status().isOk());
    }

    @Test
    void getExpenseByCategory_shouldReturnFilteredList() throws Exception {
        // Arrange
        String category = "Еда";
        when(service.getByCategory(category)).thenReturn(List.of(new Expense()));

        // Act & Assert
        mockMvc.perform(get("/expenses/category/{category}", category))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getExpenseByDate_shouldReturnList() throws Exception {
        // Arrange
        String date = "2025-12-28";
        when(service.getByDate(any(LocalDate.class))).thenReturn(List.of(new Expense()));

        // Act & Assert
        mockMvc.perform(get("/expenses/date/{date}", date))
                .andExpect(status().isOk());
    }

    @Test
    void getExpenseByPeriod_shouldReturnList() throws Exception {
        // Arrange
        String startDate = "2025-12-01";
        String endDate = "2025-12-31";
        when(service.getByPeriod(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(new Expense()));

        // Act & Assert
        mockMvc.perform(get("/expenses/period")
                        .param("startDate", startDate)
                        .param("endDate", endDate))
                .andExpect(status().isOk());
    }

    @Test
    void filterExpenses_shouldReturnFilteredList() throws Exception {
        // Arrange
        when(service.filterExpenses(any(), any(), any())).thenReturn(List.of(new Expense()));

        // Act & Assert
        mockMvc.perform(get("/expenses/filter")
                        .param("category", "Еда")
                        .param("startDate", "2025-12-01")
                        .param("endDate", "2025-12-31"))
                .andExpect(status().isOk());
    }

    @Test
    void getTotalAmount_shouldReturnNumber() throws Exception {
        // Arrange
        when(service.getTotalAmount()).thenReturn(1500.0);

        // Act & Assert
        mockMvc.perform(get("/expenses/analytics/total"))
                .andExpect(status().isOk())
                .andExpect(content().string("1500.0"));
    }

    @Test
    void getAllCategories_shouldReturnList() throws Exception {
        // Arrange
        when(service.getAllCategories()).thenReturn(List.of("Еда", "Транспорт"));

        // Act & Assert
        mockMvc.perform(get("/expenses/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("Еда"))
                .andExpect(jsonPath("$[1]").value("Транспорт"));
    }

    @Test
    void createExpense_withInvalidData_shouldReturnOk() throws Exception {
        // Arrange - пустой expense
        Expense invalidExpense = new Expense();

        when(service.save(any(Expense.class))).thenReturn(invalidExpense);

        // Act & Assert
        mockMvc.perform(post("/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidExpense)))
                .andExpect(status().isOk());
    }
}