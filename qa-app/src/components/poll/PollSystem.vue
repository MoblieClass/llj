<script setup>
import { ref } from 'vue';
import CreatePoll from './CreatePoll.vue';
import PollList from './PollList.vue';
import PollDetail from './PollDetail.vue';
import EditPoll from './EditPoll.vue';
import UserAuth from '../auth/UserAuth.vue';

const currentView = ref('list'); // 'list', 'create', 'detail', 'edit'
const selectedPoll = ref(null);
const showAuthModal = ref(false);
const currentUser = ref(null);

// 从localStorage加载用户信息
const loadUserFromStorage = () => {
  const userData = localStorage.getItem('userData');
  if (userData) {
    try {
      currentUser.value = JSON.parse(userData);
    } catch (e) {
      console.error('解析用户数据失败', e);
      localStorage.removeItem('userData');
    }
  }
};

// 初始加载用户信息
loadUserFromStorage();

// 切换视图
const switchView = (view, poll = null) => {
  // 如果未登录且要创建投票，先显示登录框
  if (view === 'create' && !currentUser.value) {
    showAuthModal.value = true;
    return;
  }
  
  currentView.value = view;
  if (poll) {
    selectedPoll.value = poll;
  }
};

// 登录成功处理
const handleLoginSuccess = (userData) => {
  currentUser.value = userData;
  localStorage.setItem('userData', JSON.stringify(userData));
  showAuthModal.value = false;
  
  // 如果之前尝试创建投票，登录后切换到创建视图
  if (currentView.value === 'list') {
    currentView.value = 'create';
  }
};

// 登出
const logout = () => {
  currentUser.value = null;
  localStorage.removeItem('userData');
  currentView.value = 'list';
};
</script>

<template>
  <div class="poll-system">
    <div class="header">
      <h1>投票系统</h1>
      
      <div class="user-area">
        <template v-if="currentUser">
          <span class="welcome-message">欢迎, {{ currentUser.username }}</span>
          <button @click="logout" class="logout-btn">登出</button>
        </template>
        <button v-else @click="showAuthModal = true" class="login-btn">
          登录/注册
        </button>
      </div>
    </div>
    
    <div class="nav-buttons">
      <button @click="switchView('list')" :class="{ active: currentView === 'list' }">
        投票列表
      </button>
      <button @click="switchView('create')" :class="{ active: currentView === 'create' }">
        创建投票
      </button>
    </div>
    
    <div class="content">
      <CreatePoll v-if="currentView === 'create'" @created="switchView('list')" />
      <PollList 
        v-if="currentView === 'list'" 
        @view-poll="switchView('detail', $event)" 
        @edit-poll="switchView('edit', $event)"
        :current-user="currentUser"
      />
      <PollDetail 
        v-if="currentView === 'detail'" 
        :poll="selectedPoll" 
        :current-user="currentUser"
        @back="switchView('list')" 
        @need-login="handleNeedLogin"
      />
      <EditPoll 
        v-if="currentView === 'edit'" 
        :poll="selectedPoll" 
        @updated="switchView('list')" 
        @back="switchView('list')" 
      />
    </div>
    
    <!-- 认证模态框 -->
    <UserAuth 
      v-if="showAuthModal" 
      @login-success="handleLoginSuccess" 
      @close="showAuthModal = false" 
    />
  </div>
</template>

<style scoped>
.poll-system {
  max-width: 800px;
  margin: 0 auto;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h1 {
  margin: 0;
}

.user-area {
  display: flex;
  align-items: center;
  gap: 10px;
}

.welcome-message {
  font-size: 14px;
  color: #666;
}

.login-btn, .logout-btn {
  background-color: #f0f0f0;
  color: #333;
  border: 1px solid #ddd;
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
}

.nav-buttons {
  display: flex;
  margin-bottom: 20px;
  gap: 10px;
}

.nav-buttons button {
  flex: 1;
}

.nav-buttons button.active {
  background-color: #646cff;
  color: white;
}

.content {
  background: #f9f9f9;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}
</style>