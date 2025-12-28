<template>
  <div class="expense-tracker">
    <h2 class="page-title">Учет расходов</h2>
    
    <!-- форма добавления -->
    <div class="form-section">
      <h3 class="section-title">Добавить расход</h3>
      <div class="form-row">
        <input v-model="newExpense.title" placeholder="Название" class="form-input" />
        <input v-model.number="newExpense.amount" placeholder="Сумма" type="number" step="1" class="form-input"/>
        <input v-model="newExpense.date" type="date" class="form-input"/>
        
        <!-- выбор категории -->
        <select v-model="newExpense.category" class="form-select">
          <option value="">Выберите категорию</option>
          <option value="Еда">еда</option>
          <option value="Транспорт">транспорт</option>
          <option value="Покупки">покупки</option>
          <option value="Развлечения">развлечения</option>
          <option value="Здоровье">здоровье</option>
          <option value="Образование">образование</option>
          <option value="other">другое</option>
        </select>
        
        <!-- если выбрано другое-->
        <input v-if="newExpense.category === 'other'" 
               v-model="newExpense.customCategory" 
               placeholder="Введите свою категорию"
               class="form-input"/>
        
        <input v-model="newExpense.comment" placeholder="Комментарий (необязательно)" class="form-input"/>
        <button @click="addExpense" class="btn btn-add">добавить</button>
      </div>
    </div>
    
    <!-- фильтры -->
    <div class="filter-section">
      <h3 class="section-title">Фильтры</h3>
      <div class="filter-row">
        <select v-model="selectedCategory" @change="applyFilters" class="form-select">
          <option value="">Все категории</option>
          <option v-for="cat in allCategories" :key="cat" :value="cat">
            {{ cat }}
          </option>
        </select>
        
        <div class="date-filter">
          <label>от: 
            <input v-model="startDate" type="date" @change="applyFilters" class="form-input date-input"/>
          </label>
          <label>до: 
            <input v-model="endDate" type="date" @change="applyFilters" class="form-input date-input"/>
          </label>
        </div>
        
        <button @click="clearFilters" class="btn btn-clear">очистить фильтры</button>
      </div>
    </div>
    
    <!-- аналитика -->
    <div class="analytics-section" v-if="analytics.length > 0">
      <h3 class="section-title">Аналитика по категориям</h3>
      <div class="analytics-grid">
        <div v-for="item in analytics" :key="item.category" class="analytics-item">
          <span class="category-label">{{ item.category }}</span>
          <span class="category-amount">{{ item.total.toFixed(2) }} ₽</span>
        </div>
      </div>
      <div class="total-summary">
        <strong>итого: {{ totalAmount.toFixed(2) }} ₽</strong>
      </div>
    </div>
    
    <!-- список расходов -->
    <div class="list-section">
      <h3 class="section-title">Список расходов ({{ filteredExpenses.length }})</h3>
      
      <div v-if="filteredExpenses.length === 0" class="empty-state">
        нет расходов для отображения
      </div>
      
      <div v-else class="expenses-list">
        <div v-for="expense in filteredExpenses" :key="expense.id" class="expense-card">
          <div class="expense-details">
            <div class="expense-header">
              <div class="expense-title">{{ expense.title }}</div>
              <div class="expense-amount-large">{{ expense.amount.toFixed(2) }} ₽</div>
            </div>
            <div class="expense-meta">
              <span class="expense-category">{{ expense.category || 'без категории' }}</span>
              <span class="expense-date">{{ formatDate(expense.date) }}</span>
            </div>
            <div v-if="expense.comment" class="expense-comment">{{ expense.comment }}</div>
          </div>
          <button @click="deleteExpense(expense.id)" class="btn-delete">Удалить</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ExpensesList',
  data() {
    return {
      expenses: [],           // все расходы из бд
      allCategories: [],      // все уникальные категории из бд
      
      // новая запись
      newExpense: {
        title: '',
        amount: 0,
        date: new Date().toISOString().split('T')[0], // сегодняшняя дата
        category: '',
        customCategory: '',
        comment: ''
      },
      
      // фильтры
      selectedCategory: '',
      startDate: '',
      endDate: '',
      
      // аналитика
      analytics: []
    }
  },
  computed: {
    // отфильтрованные расходы (рассчитываются автоматически)
    filteredExpenses() {
      let filtered = this.expenses;
      
      // фильтр по категории
      if (this.selectedCategory) {
        filtered = filtered.filter(e => 
          e.category === this.selectedCategory || 
          (!e.category && this.selectedCategory === 'без категории')
        );
      }
      
      // фильтр по дате
      if (this.startDate) {
        const start = new Date(this.startDate);
        filtered = filtered.filter(e => new Date(e.date) >= start);
      }
      
      if (this.endDate) {
        const end = new Date(this.endDate);
        filtered = filtered.filter(e => new Date(e.date) <= end);
      }
      
      // сортируем по дате (новые сверху)
      return filtered.sort((a, b) => new Date(b.date) - new Date(a.date));
    },
    
    // общая сумма отфильтрованных расходов
    totalAmount() {
      return this.filteredExpenses.reduce((sum, expense) => sum + expense.amount, 0);
    }
  },
  methods: {
    // загрузить все расходы
    async fetchExpenses() {

        const savedUser = localStorage.getItem('currentUser')
  if (!savedUser) {
    console.error('пользователь не авторизован')
    return
  }
  
      try {
        const response = await fetch('http://localhost:8080/expenses');
        this.expenses = await response.json();
        this.updateAnalytics();
        this.fetchCategories();
      } catch (error) {
        console.error('ошибка при загрузке расходов:', error);
      }
    },
    
    // загрузить все категории
    async fetchCategories() {
      try {
        const response = await fetch('http://localhost:8080/expenses/categories');
        const categories = await response.json();
        this.allCategories = categories.filter(cat => cat && cat.trim() !== '');
      } catch (error) {
        console.error('ошибка при загрузке категорий:', error);
      }
    },
    
    // добавить новый расход
    async addExpense() {
      // определяем категорию
      let category = this.newExpense.category;
      if (category === 'other') {
        category = this.newExpense.customCategory.trim();
        if (!category) {
          alert('введите название категории');
          return;
        }
      }
      
      // проверяем обязательные поля
      if (!this.newExpense.title || !this.newExpense.amount) {
        alert('заполните название и сумму');
        return;
      }
      
      // создаем объект для отправки
      const expenseData = {
        title: this.newExpense.title,
        amount: parseFloat(this.newExpense.amount),
        date: this.newExpense.date,
        category: category,
        comment: this.newExpense.comment
      };
      
      try {
        const response = await fetch('http://localhost:8080/expenses', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(expenseData)
        });
        
        const newExpense = await response.json();
        this.expenses.push(newExpense);
        this.resetForm();
        this.updateAnalytics();
      } catch (error) {
        console.error('ошибка при добавлении расхода:', error);
      }
    },
    
    // удалить расход
    async deleteExpense(id) {
      if (confirm('удалить эту запись?')) {
        try {
          await fetch(`http://localhost:8080/expenses/${id}`, {
            method: 'DELETE'
          });
          
          this.expenses = this.expenses.filter(e => e.id !== id);
          this.updateAnalytics();
        } catch (error) {
          console.error('ошибка при удалении:', error);
        }
      }
    },
    
    // обновить аналитику
    async updateAnalytics() {
      try {
        const response = await fetch('http://localhost:8080/expenses/analytics/category');
        const data = await response.json();
        
        // преобразуем объект в массив
        this.analytics = Object.entries(data).map(([category, total]) => ({
          category: category || 'без категории',
          total
        }));
      } catch (error) {
        console.error('ошибка при загрузке аналитики:', error);
      }
    },
    
    // применить фильтры
    applyFilters() {
      // фильтры применяются автоматически через computed свойство
      this.updateAnalytics();
    },
    
    // очистить фильтры
    clearFilters() {
      this.selectedCategory = '';
      this.startDate = '';
      this.endDate = '';
    },
    
    // сбросить форму
    resetForm() {
      this.newExpense = {
        title: '',
        amount: 0,
        date: new Date().toISOString().split('T')[0],
        category: '',
        customCategory: '',
        comment: ''
      };
    },
    
    // форматировать дату
    formatDate(dateString) {
      const date = new Date(dateString);
      return date.toLocaleDateString('ru-RU');
    }
  },
  mounted() {
    this.fetchExpenses();
  }
}
</script>

<style scoped>
.expense-tracker {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.page-title {
  color: #5d6c7d;
  text-align: center;
  margin-bottom: 30px;
  font-weight: 300;
  font-size: 2rem;
}

.section-title {
  color: #6a7b8c;
  margin-bottom: 15px;
  font-weight: 400;
  font-size: 1.3rem;
}

/* базовые стили */
.form-section, .filter-section, .analytics-section, .list-section {
  background: linear-gradient(135deg, #f8f9fa 0%, #f1f3f4 100%);
  border-radius: 12px;
  padding: 25px;
  margin-bottom: 25px;
  border: 1px solid #e3e7ea;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
}

.form-row, .filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.form-input, .form-select {
  padding: 10px 14px;
  border: 1px solid #d1d9e0;
  border-radius: 8px;
  font-size: 15px;
  background: white;
  color: #4a5568;
  transition: all 0.2s ease;
}

.form-input:focus, .form-select:focus {
  outline: none;
  border-color: #a0b3c6;
  box-shadow: 0 0 0 3px rgba(160, 179, 198, 0.1);
}

.date-filter {
  display: flex;
  gap: 15px;
  align-items: center;
}

.date-input {
  width: 140px;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 15px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.btn:active {
  transform: translateY(0);
}

.btn-add {
  background: linear-gradient(135deg, #89c2d9 0%, #6c9bb8 100%);
  color: white;
}

.btn-add:hover {
  background: linear-gradient(135deg, #7ab4cd 0%, #5d8aa6 100%);
}

.btn-clear {
  background: linear-gradient(135deg, #f4a261 0%, #e76f51 100%);
  color: white;
}

.btn-clear:hover {
  background: linear-gradient(135deg, #e39452 0%, #d66548 100%);
}

.btn-delete {
  background: linear-gradient(135deg, #ff6b6b 0%, #ff4444 100%);
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 15px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.btn-delete:hover {
  background: linear-gradient(135deg, #ff5252 0%, #ff3333 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(255, 68, 68, 0.2);
}


/* аналитика */
.analytics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 15px;
  margin: 20px 0;
}

.analytics-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background: white;
  border-radius: 10px;
  border-left: 5px solid #a0b3c6;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  transition: transform 0.2s ease;
}

.analytics-item:hover {
  transform: translateX(5px);
}

.category-label {
  font-weight: 500;
  color: #5d6c7d;
}

.category-amount {
  font-weight: 600;
  color: #4a5568;
}

.total-summary {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 2px solid #e3e7ea;
  font-size: 18px;
  color: #5d6c7d;
  text-align: right;
}

/* список расходов */
.empty-state {
  text-align: center;
  padding: 50px;
  color: #9ca3af;
  font-style: italic;
  background: white;
  border-radius: 10px;
  font-size: 16px;
}

.expenses-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.expense-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 3px 6px rgba(0, 0, 0, 0.08);
  transition: all 0.2s ease;
  border-left: 4px solid #a0b3c6;
}

.expense-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 12px rgba(0, 0, 0, 0.1);
}

.expense-details {
  flex: 1;
}

.expense-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.expense-title {
  font-weight: 600;
  font-size: 17px;
  color: #4a5568;
}

.expense-amount-large {
  font-weight: 700;
  font-size: 18px;
  color: #e76f51;
}

.expense-meta {
  display: flex;
  gap: 15px;
  align-items: center;
  margin-bottom: 5px;
}

.expense-category {
  display: inline-block;
  background: linear-gradient(135deg, #f1faee 0%, #e3f2e8 100%);
  padding: 4px 12px;
  border-radius: 15px;
  font-size: 13px;
  color: #5d6c7d;
  border: 1px solid #d1e7dd;
}

.expense-date {
  color: #8a9ba8;
  font-size: 14px;
}

.expense-comment {
  color: #7d8c9d;
  font-style: italic;
  margin-top: 8px;
  font-size: 14px;
  padding-left: 5px;
  border-left: 3px solid #e3e7ea;
}

/* адаптивность */
@media (max-width: 768px) {
  .form-row, .filter-row {
    flex-direction: column;
    align-items: stretch;
  }
  
  .date-filter {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
  }
  
  .date-input {
    width: 100%;
  }
  
  .expense-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 5px;
  }
  
  .expense-card {
    flex-direction: column;
    align-items: stretch;
    gap: 15px;
  }
  
  .btn-delete {
    align-self: flex-end;
  }
}
</style>