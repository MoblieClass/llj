<script setup>
import { ref, computed } from 'vue';
import { generateCSV } from '../utils/exportHelpers';

const props = defineProps({
  poll: {
    type: Object,
    required: true
  }
});

const emit = defineEmits(['back']);

// 计算总投票数
const totalVotes = computed(() => {
  return props.poll.options.reduce((sum, option) => sum + option.votes, 0);
});

// 计算每个选项的百分比
const getPercentage = (votes) => {
  if (totalVotes.value === 0) return 0;
  return Math.round((votes / totalVotes.value) * 100);
};

// 检查用户是否已投票
const hasUserVoted = computed(() => {
  // 如果没有登录，则未投票
  if (!props.currentUser) return false;
  
  // 检查localStorage中的投票记录
  const votedPolls = JSON.parse(localStorage.getItem('votedPolls') || '[]');
  return votedPolls.includes(localPoll.value.id);
});


// 提交投票
const submitVote = async (optionId) => {
  // 如果未登录，提示登录
  if (!props.currentUser) {
    errorMessage.value = '请先登录再进行投票';
    return;
  }
  
  // 如果已经投过票，不允许再次投票
  if (hasUserVoted.value) {
    errorMessage.value = '您已经对此投票进行了投票';
    return;
  }
  
  votingStatus.value = 'loading';
  
  try {
    const response = await fetch(`http://localhost:8080/api/polls/${localPoll.value.id}/vote`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ optionId }),
      credentials: 'include'
    });
    
    if (response.ok) {
      const updatedPoll = await response.json();
      localPoll.value = updatedPoll;
      votingStatus.value = 'success';
      
      // 记录用户已投票
      const votedPolls = JSON.parse(localStorage.getItem('votedPolls') || '[]');
      votedPolls.push(localPoll.value.id);
      localStorage.setItem('votedPolls', JSON.stringify(votedPolls));
      
    } else {
      const error = await response.json();
      errorMessage.value = error.message || '投票失败，请重试';
      votingStatus.value = 'error';
    }
  } catch (error) {
    console.error('投票出错:', error);
    errorMessage.value = '投票时发生错误，请重试';
    votingStatus.value = 'error';
  }
};

// 清除错误消息
const clearError = () => {
  errorMessage.value = '';
};

// 导出为CSV
const exportToCSV = () => {
  const headers = ['选项', '票数', '百分比'];
  const data = props.poll.options.map(option => [
    option.content,
    option.votes,
    `${getPercentage(option.votes)}%`
  ]);
  
  // 添加总计行
  data.push(['总计', totalVotes.value, '100%']);
  
  const csvContent = generateCSV(headers, data);
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
  const link = document.createElement('a');
  const url = URL.createObjectURL(blob);
  
  link.setAttribute('href', url);
  link.setAttribute('download', `投票结果-${props.poll.title}.csv`);
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
};

// 打印结果
const printResults = () => {
  const printWindow = window.open('', '_blank');
  
  printWindow.document.write(`
    <html>
      <head>
        <title>投票结果: ${props.poll.title}</title>
        <style>
          body { font-family: Arial, sans-serif; padding: 20px; }
          h1 { color: #333; }
          .description { color: #666; margin-bottom: 20px; }
          table { width: 100%; border-collapse: collapse; }
          th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
          th { background-color: #f5f5f5; }
          .bar-container { width: 200px; background-color: #eee; border-radius: 4px; }
          .bar { height: 20px; background-color: #4CAF50; border-radius: 4px; }
          .total-row { font-weight: bold; }
          @media print {
            button { display: none; }
            body { padding: 0; }
          }
        </style>
      </head>
      <body>
        <h1>${props.poll.title}</h1>
        <p class="description">${props.poll.description || '无描述'}</p>
        
        <table>
          <thead>
            <tr>
              <th>选项</th>
              <th>票数</th>
              <th>百分比</th>
              <th>比例图</th>
            </tr>
          </thead>
          <tbody>
            ${props.poll.options.map(option => `
              <tr>
                <td>${option.content}</td>
                <td>${option.votes}</td>
                <td>${getPercentage(option.votes)}%</td>
                <td>
                  <div class="bar-container">
                    <div class="bar" style="width: ${getPercentage(option.votes)}%;"></div>
                  </div>
                </td>
              </tr>
            `).join('')}
            <tr class="total-row">
              <td>总计</td>
              <td>${totalVotes.value}</td>
              <td>100%</td>
              <td></td>
            </tr>
          </tbody>
        </table>
        
        <div style="margin-top: 30px;">
          <p>创建日期: ${new Date(props.poll.createdAt).toLocaleDateString()}</p>
          <p>结束日期: ${props.poll.endDate ? new Date(props.poll.endDate).toLocaleDateString() : '无'}</p>
          <p>状态: ${props.poll.isActive ? '进行中' : '已结束'}</p>
        </div>
        
        <button onclick="window.print()">打印此页</button>
      </body>
    </html>
  `);
  
  printWindow.document.close();
  printWindow.focus();
};
</script>

<template>
  <div class="poll-detail">
    <div class="back-button">
      <button @click="emit('back')" class="back-btn">返回列表</button>
    </div>
    
    <h2>{{ poll.title }}</h2>
    <p class="description">{{ poll.description || '无描述' }}</p>
    
    <div class="meta-info">
      <div class="status" :class="{ active: poll.isActive }">
        {{ poll.isActive ? '进行中' : '已结束' }}
      </div>
      <div>创建日期: {{ new Date(poll.createdAt).toLocaleDateString() }}</div>
      <div>结束日期: {{ poll.endDate ? new Date(poll.endDate).toLocaleDateString() : '无' }}</div>
    </div>

    
    <div v-if="localPoll.active && !hasUserVoted && props.currentUser" class="voting-area">
      <h3>请选择一个选项进行投票</h3>
      
      <div v-if="errorMessage" class="error-message" @click="clearError">
        {{ errorMessage }}
      </div>
      
      <div v-for="option in localPoll.options" :key="option.id" class="option-vote">
        <button 
          @click="submitVote(option.id)" 
          :disabled="votingStatus === 'loading'"
          class="vote-btn"
        >
          {{ option.content }}
        </button>
      </div>
      
      <div v-if="votingStatus === 'success'" class="success-message">
        投票成功！感谢您的参与。
      </div>
    </div>
    
    <!-- 未登录提示 -->
    <div v-if="localPoll.active && !props.currentUser" class="login-prompt">
      <p>请<button @click="$emit('need-login')" class="inline-btn">登录</button>后参与投票</p>
    </div>
    
    <div class="stats-header">
      <h3>投票结果</h3>
      <div>总投票数: <strong>{{ totalVotes }}</strong></div>
    </div>


    
    <div class="results">
      <div v-for="option in poll.options" :key="option.id" class="result-item">
        <div class="result-info">
          <div class="option-content">{{ option.content }}</div>
          <div class="votes-info">
            <span class="votes-number">{{ option.votes }} 票</span>
            <span class="votes-percentage">{{ getPercentage(option.votes) }}%</span>
          </div>
        </div>
        
        <div class="progress-bar-container">
          <div 
            class="progress-bar" 
            :style="{ width: `${getPercentage(option.votes)}%` }"
          ></div>
        </div>
      </div>
    </div>
    
    <div class="action-buttons">
      <button @click="exportToCSV" class="export-btn">
        导出结果 (CSV)
      </button>
      <button @click="printResults" class="print-btn">
        打印结果
      </button>
    </div>
  </div>
</template>

<style scoped>
.poll-detail {
  padding: 20px;
}

.back-button {
  margin-bottom: 20px;
}

.back-btn {
  background-color: #f0f0f0;
  color: #333;
}

.description {
  color: #666;
  margin-bottom: 20px;
  font-size: 16px;
}

.meta-info {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: #666;
  margin-bottom: 30px;
}

.status {
  padding: 3px 10px;
  border-radius: 12px;
  background-color: #ccc;
  color: white;
}

.status.active {
  background-color: #2ed573;
}

.stats-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.stats-header h3 {
  margin: 0;
}

.results {
  margin-bottom: 30px;
}

.result-item {
  margin-bottom: 15px;
}

.result-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
}

.option-content {
  font-weight: 500;
}

.votes-info {
  display: flex;
  gap: 10px;
}

.votes-number {
  color: #666;
}

.votes-percentage {
  font-weight: bold;
}

.progress-bar-container {
  height: 20px;
  background-color: #eee;
  border-radius: 10px;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  background-color: #4CAF50;
  transition: width 0.5s ease;
}

.action-buttons {
  display: flex;
  gap: 10px;
  margin-top: 30px;
  justify-content: center;
}

.export-btn {
  background-color: #3498db;
  color: white;
}

.print-btn {
  background-color: #9b59b6;
  color: white;
}
</style>


<style scoped>
.poll-detail {
  padding: 20px;
}

.back-button {
  margin-bottom: 20px;
}

.back-btn {
  background-color: #f0f0f0;
  color: #333;
}

.description {
  color: #666;
  margin-bottom: 20px;
  font-size: 16px;
}

.meta-info {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: #666;
  margin-bottom: 30px;
}

.status {
  padding: 3px 10px;
  border-radius: 12px;
  background-color: #ccc;
  color: white;
}

.status.active {
  background-color: #2ed573;
}

.voting-area {
  margin-bottom: 30px;
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background-color: #f9f9f9;
}

.option-vote {
  margin-bottom: 10px;
}

.vote-btn {
  width: 100%;
  padding: 10px;
  background-color: #fff;
  border: 1px solid #ddd;
  border-radius: 8px;
  text-align: left;
  font-size: 16px;
  transition: background-color 0.2s;
}

.vote-btn:hover {
  background-color: #f0f0f0;
}

.error-message {
  background-color: #ff6b6b;
  color: white;
  padding: 10px;
  border-radius: 4px;
  margin-bottom: 15px;
  cursor: pointer;
}

.success-message {
  background-color: #2ed573;
  color: white;
  padding: 10px;
  border-radius: 4px;
  margin-top: 15px;
}

.login-prompt {
  text-align: center;
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f0f0f0;
  border-radius: 8px;
}

.inline-btn {
  background: none;
  border: none;
  color: #4a6cf7;
  padding: 0;
  font-size: inherit;
  cursor: pointer;
  text-decoration: underline;
}

.stats-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.stats-header h3 {
  margin: 0;
}

.results {
  margin-bottom: 30px;
}

.result-item {
  margin-bottom: 15px;
}

.result-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
}

.option-content {
  font-weight: 500;
}

.votes-info {
  display: flex;
  gap: 10px;
}

.votes-number {
  color: #666;
}

.votes-percentage {
  font-weight: bold;
}

.progress-bar-container {
  height: 20px;
  background-color: #eee;
  border-radius: 10px;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  background-color: #4CAF50;
  transition: width 0.5s ease;
}
</style>