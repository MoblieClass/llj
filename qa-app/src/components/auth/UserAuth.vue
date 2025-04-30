<script setup>
import { ref, computed } from 'vue';

const emit = defineEmits(['login-success', 'close']);

const isLoginMode = ref(true);
const username = ref('');
const password = ref('');
const errorMessage = ref('');
const loading = ref(false);

// 切换登录/注册模式
const toggleMode = () => {
  isLoginMode.value = !isLoginMode.value;
  errorMessage.value = '';
};

const formTitle = computed(() => isLoginMode.value ? '用户登录' : '用户注册');
const submitText = computed(() => isLoginMode.value ? '登录' : '注册');
const toggleText = computed(() => isLoginMode.value ? '没有账号？点击注册' : '已有账号？点击登录');

const handleSubmit = async () => {
  if (!username.value.trim() || !password.value.trim()) {
    errorMessage.value = '用户名和密码不能为空';
    return;
  }
  
  loading.value = true;
  
  try {
    // 确定API端点
    const endpoint = isLoginMode.value ? 'login' : 'register';
    
    // 发送请求
    const response = await fetch(`http://localhost:8080/api/auth/${endpoint}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        username: username.value,
        password: password.value,
      }),
      credentials: 'include' // 保存cookie
    });
    
    if (response.ok) {
      const userData = await response.json();
      // 通知父组件登录成功
      emit('login-success', userData);
    } else {
      const error = await response.json();
      errorMessage.value = error.message || (isLoginMode.value ? '登录失败' : '注册失败');
    }
  } catch (error) {
    console.error('认证错误:', error);
    errorMessage.value = '服务器连接失败，请稍后重试';
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <div class="auth-modal">
    <div class="auth-container">
      <div class="auth-header">
        <h2>{{ formTitle }}</h2>
        <button @click="emit('close')" class="close-btn">&times;</button>
      </div>
      
      <div v-if="errorMessage" class="error-message">
        {{ errorMessage }}
      </div>
      
      <form @submit.prevent="handleSubmit">
        <div class="form-group">
          <label for="username">用户名</label>
          <input 
            id="username" 
            v-model="username" 
            type="text" 
            placeholder="输入用户名" 
            required
          />
        </div>
        
        <div class="form-group">
          <label for="password">密码</label>
          <input 
            id="password" 
            v-model="password" 
            type="password" 
            placeholder="输入密码" 
            required
          />
        </div>
        
        <div class="actions">
          <button 
            type="submit" 
            class="submit-btn" 
            :disabled="loading"
          >
            {{ loading ? '处理中...' : submitText }}
          </button>
        </div>
      </form>
      
      <div class="toggle-container">
        <button @click="toggleMode" class="toggle-btn">
          {{ toggleText }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.auth-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.auth-container {
  width: 400px;
  background-color: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}

.auth-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.auth-header h2 {
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #666;
}

.error-message {
  background-color: #ff6b6b;
  color: white;
  padding: 10px;
  border-radius: 4px;
  margin-bottom: 15px;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: 500;
}

input {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.actions {
  margin-top: 20px;
}

.submit-btn {
  width: 100%;
  padding: 10px;
  background-color: #4a6cf7;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
}

.submit-btn:disabled {
  background-color: #a0a0a0;
  cursor: not-allowed;
}

.toggle-container {
  margin-top: 15px;
  text-align: center;
}

.toggle-btn {
  background: none;
  border: none;
  color: #4a6cf7;
  cursor: pointer;
  font-size: 14px;
}
</style>