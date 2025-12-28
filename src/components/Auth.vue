
<template>
  <div class="auth-container">
    <div class="auth-card">
      <h2 class="auth-title">{{ isLoginMode ? 'вход' : 'регистрация' }}</h2>
      
      <form @submit.prevent="handleSubmit" class="auth-form">
        <div class="form-group">
          <input
            v-model="form.username"
            type="text"
            placeholder="имя пользователя"
            required
            class="form-input"
          />
        </div>
        
        <div v-if="!isLoginMode" class="form-group">
          <input
            v-model="form.email"
            type="email"
            placeholder="email (необязательно)"
            class="form-input"
          />
        </div>
        
        <div class="form-group">
          <input
            v-model="form.password"
            type="password"
            placeholder="пароль"
            required
            class="form-input"
          />
        </div>
        
        <div v-if="!isLoginMode" class="form-group">
          <input
            v-model="form.confirmPassword"
            type="password"
            placeholder="подтвердите пароль"
            required
            class="form-input"
          />
        </div>
        
        <div v-if="error" class="error-message">
          {{ error }}
        </div>
        
        <button type="submit" class="btn btn-auth" :disabled="loading">
          {{ loading ? 'загрузка...' : (isLoginMode ? 'войти' : 'зарегистрироваться') }}
        </button>
        
        <div class="auth-switch">
          <span>{{ isLoginMode ? 'нет аккаунта?' : 'уже есть аккаунт?' }}</span>
          <button type="button" @click="toggleMode" class="btn-link">
            {{ isLoginMode ? 'зарегистрироваться' : 'войти' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Auth',
  emits: ['login-success'],
  data() {
    return {
      isLoginMode: true,
      loading: false,
      error: '',
      form: {
        username: '',
        password: '',
        confirmPassword: '',
        email: ''
      }
    }
  },
  methods: {
    toggleMode() {
      this.isLoginMode = !this.isLoginMode
      this.error = ''
      this.form.confirmPassword = ''
    },
    
    async handleSubmit() {
      this.loading = true
      this.error = ''
      
      try {
        // Валидация
        if (!this.isLoginMode && this.form.password !== this.form.confirmPassword) {
          throw new Error('пароли не совпадают')
        }
        
        if (this.form.username.length < 3) {
          throw new Error('имя пользователя должно быть не менее 3 символов')
        }
        
        if (this.form.password.length < 4) {
          throw new Error('пароль должен быть не менее 4 символов')
        }
        
        const url = this.isLoginMode ? '/api/auth/login' : '/api/auth/register'
        const payload = this.isLoginMode 
          ? { username: this.form.username, password: this.form.password }
          : { 
              username: this.form.username, 
              password: this.form.password,
              email: this.form.email || ''
            }
        
        const response = await fetch(`http://localhost:8080${url}`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        })
        
        const data = await response.json()
        
        if (!response.ok) {
          throw new Error(data.error || 'ошибка при выполнении запроса')
        }
        
        // Сохраняем информацию о пользователе
        localStorage.setItem('currentUser', JSON.stringify(data.user))
        
        // Уведомляем родительский компонент
        this.$emit('login-success', data.user)
        
        // Очищаем форму
        this.resetForm()
        
      } catch (error) {
        this.error = error.message
      } finally {
        this.loading = false
      }
    },
    
    resetForm() {
      this.form = {
        username: '',
        password: '',
        confirmPassword: '',
        email: ''
      }
    }
  },
  
  mounted() {
    // Проверяем, есть ли сохраненный пользователь
    const savedUser = localStorage.getItem('currentUser')
    if (savedUser) {
      this.$emit('login-success', JSON.parse(savedUser))
    }
  }
}
</script>

<style scoped>
.auth-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #f8f9fa 0%, #f1f3f4 100%);
  padding: 20px;
}

.auth-card {
  background: white;
  border-radius: 12px;
  padding: 40px;
  width: 100%;
  max-width: 400px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.auth-title {
  color: #5d6c7d;
  text-align: center;
  margin-bottom: 30px;
  font-weight: 400;
  font-size: 1.8rem;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-input {
  padding: 12px 16px;
  border: 1px solid #d1d9e0;
  border-radius: 8px;
  font-size: 15px;
  background: white;
  color: #4a5568;
  transition: all 0.2s ease;
}

.form-input:focus {
  outline: none;
  border-color: #89c2d9;
  box-shadow: 0 0 0 3px rgba(137, 194, 217, 0.1);
}

.error-message {
  color: #ff4444;
  background: #ffebee;
  padding: 10px;
  border-radius: 6px;
  font-size: 14px;
  text-align: center;
}

.btn-auth {
  background: linear-gradient(135deg, #89c2d9 0%, #6c9bb8 100%);
  color: white;
  padding: 14px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 500;
  transition: all 0.2s ease;
  width: 100%;
}

.btn-auth:hover:not(:disabled) {
  background: linear-gradient(135deg, #7ab4cd 0%, #5d8aa6 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.btn-auth:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.auth-switch {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  margin-top: 20px;
  color: #6a7b8c;
  font-size: 14px;
}

.btn-link {
  background: none;
  border: none;
  color: #89c2d9;
  cursor: pointer;
  font-size: 14px;
  text-decoration: underline;
  padding: 0;
}

.btn-link:hover {
  color: #6c9bb8;
}
</style>