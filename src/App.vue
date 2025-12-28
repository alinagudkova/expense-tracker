<!-- src/App.vue -->
<template>
  <div>
    <header v-if="currentUser" class="app-header">
      <div class="header-content">
        <h1>учет расходов</h1>
        <div class="user-info">
          <span class="username">{{ currentUser.username }}</span>
          <button @click="logout" class="btn-logout">выйти</button>
        </div>
      </div>
    </header>
    
    <main>
      <Auth 
        v-if="!currentUser" 
        @login-success="handleLoginSuccess"
      />
      <ExpensesList v-else />
    </main>
  </div>
</template>

<script>
import Auth from './components/Auth.vue'
import ExpensesList from './components/ExpensesList.vue'

export default {
  name: 'App',
  components: {
    Auth,
    ExpensesList
  },
  data() {
    return {
      currentUser: null
    }
  },
  methods: {
    handleLoginSuccess(user) {
      this.currentUser = user
    },
    
    async logout() {
      try {
        await fetch('http://localhost:8080/api/auth/logout', {
          method: 'POST'
        })
      } catch (error) {
        console.error('ошибка при выходе:', error)
      }
      
      localStorage.removeItem('currentUser')
      this.currentUser = null
    }
  },
  mounted() {
    // Проверяем текущего пользователя
    const savedUser = localStorage.getItem('currentUser')
    if (savedUser) {
      this.currentUser = JSON.parse(savedUser)
    }
  }
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background: #f8f9fa;
  color: #4a5568;
}

.app-header {
  background: linear-gradient(135deg, #5d6c7d 0%, #4a5568 100%);
  color: white;
  padding: 20px 0;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.header-content h1 {
  font-weight: 300;
  font-size: 1.8rem;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.username {
  font-weight: 500;
}

.btn-logout {
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s ease;
}

.btn-logout:hover {
  background: rgba(255, 255, 255, 0.2);
}

main {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
</style>