<script setup>
import { ref, onMounted,computed } from 'vue';

const emit = defineEmits(['view-poll', 'edit-poll']);
const polls = ref([]);
const loading = ref(true);
const error = ref(null);
const searchQuery = ref('');

// 获取所有投票
const fetchPolls = async () => {
  loading.value = true;
  error.value = null;
  
  try {
    // 实际项目中，这里应当是真实的API调用
    const response = await fetch('http://localhost:8080/api/polls',{method: 'GET'});
    const response_json  = await response.json();
    console.log('获取投票列表:', response_json); // 调试用
    polls.value = response_json.map(poll => ({
      ...poll, // 保留原有属性
      isActive: poll.active  // 添加isActive属性与active保持一致
    }));
    loading.value = false;
  } catch (err) {
    console.error('获取投票列表失败:', err);
    error.value = '获取投票列表失败，请重试';
    loading.value = false;
  }
};

// 删除投票
const deletePoll = async (pollId) => {
  if (!confirm('确定要删除这个投票吗？此操作无法撤销。')) {
    return;
  }
  
  try {
    // 实际项目中，这里应当是真实的API调用
    // await fetch(`/api/polls/${pollId}`, { method: 'DELETE' });
    
    // 模拟删除，实际应该调用API
    polls.value = polls.value.filter(poll => poll.id !== pollId);
    
  } catch (err) {
    console.error('删除投票失败:', err);
    alert('删除投票失败，请重试');
  }
};

// 根据搜索查询过滤投票
const filteredPolls = computed(() => {
  if (!searchQuery.value) return polls.value;
  
  const query = searchQuery.value.toLowerCase();
  return polls.value.filter(poll => 
    poll.title.toLowerCase().includes(query) || 
    poll.description.toLowerCase().includes(query)
  );
});

// 格式化日期显示
const formatDate = (dateString) => {
  if (!dateString) return '无结束日期';
  const date = new Date(dateString);
  return date.toLocaleDateString('zh-CN');
};

// 页面加载时获取投票列表
onMounted(fetchPolls);
</script>

<template>
  <div class="poll-list">
    <div class="list-header">
      <h2>投票列表</h2>
      <div class="search-box">
        <input
          v-model="searchQuery"
          type="text"
          placeholder="搜索投票..."
          class="search-input"
        />
      </div>
    </div>
    
    <div v-if="loading" class="loading">
      加载中...
    </div>
    
    <div v-else-if="error" class="error">
      {{ error }}
      <button @click="fetchPolls" class="retry-btn">重试</button>
    </div>
    
    <div v-else-if="polls.length === 0" class="empty-state">
      暂无投票。点击"创建投票"按钮创建新的投票。
    </div>
    
    <div v-else class="polls-container">
      <div v-for="poll in filteredPolls" :key="poll.id" class="poll-card">
        <div class="poll-card-header">
          <h3>{{ poll.title }}</h3>
          <div class="poll-status" :class="{ active: poll.isActive }">
            {{ poll.isActive ? '进行中' : '已结束' }}
          </div>
        </div>
        
        <p class="poll-description">{{ poll.description || '无描述' }}</p>
        
        <div class="poll-meta">
          <div class="poll-date">
            <span>创建日期: {{ formatDate(poll.createdAt) }}</span>
            <span>结束日期: {{ formatDate(poll.endDate) }}</span>
          </div>
          <div class="poll-options-count">
            {{ poll.options.length }} 个选项 | 
            总票数: {{ poll.options.reduce((sum, opt) => sum + opt.votes, 0) }}
          </div>
        </div>
        
        <div class="poll-actions">
          <button @click="emit('view-poll', poll)" class="view-btn">
            查看详情
          </button>
          <button @click="emit('edit-poll', poll)" class="edit-btn">
            编辑
          </button>
          <button @click="deletePoll(poll.id)" class="delete-btn">
            删除
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.poll-list {
  width: 100%;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-input {
  padding: 8px 12px;
  border-radius: 4px;
  border: 1px solid #ddd;
  width: 250px;
}

.loading, .error, .empty-state {
  text-align: center;
  padding: 40px 0;
  color: #666;
}

.retry-btn {
  margin-top: 10px;
  background-color: #f0f0f0;
}

.polls-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.poll-card {
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 15px;
  background-color: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  transition: transform 0.2s, box-shadow 0.2s;
}

.poll-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.poll-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
}

.poll-card-header h3 {
  margin: 0;
  font-size: 18px;
}

.poll-status {
  font-size: 12px;
  padding: 3px 8px;
  border-radius: 12px;
  color: white;
  background-color: #ccc;
}

.poll-status.active {
  background-color: #2ed573;
}

.poll-description {
  color: #666;
  margin-bottom: 15px;
  font-size: 14px;
  height: 40px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.poll-meta {
  font-size: 12px;
  color: #888;
  margin-bottom: 15px;
}

.poll-date {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
}

.poll-actions {
  display: flex;
  gap: 8px;
}

.view-btn {
  background-color: #1e90ff;
  color: white;
  flex: 1;
}

.edit-btn {
  background-color: #f39c12;
  color: white;
}

.delete-btn {
  background-color: #ff4757;
  color: white;
}
</style>