<script setup>
import { ref } from 'vue';

const emit = defineEmits(['created']);

const title = ref('');
const description = ref('');
const options = ref([{ content: '' }, { content: '' }]);
const endDate = ref('');
const startDate = ref('');
const errorMessage = ref('');

const addOption = () => {
  options.value.push({ content: '' });
};

const removeOption = (index) => {
  if (options.value.length > 2) {
    options.value.splice(index, 1);
  } else {
    errorMessage.value = '至少需要两个选项';
    setTimeout(() => {
      errorMessage.value = '';
    }, 3000);
  }
};

// 日期时间处理 - 格式转换为ISO 8601
const formatDateTime = (dateTimeString) => {
  if (!dateTimeString) return null;
  // 将本地日期时间字符串转换为ISO 8601格式
  const date = new Date(dateTimeString);
  return date.toISOString();
};

const createPoll = async () => {
  // 表单验证
  if (!title.value.trim()) {
    errorMessage.value = '请输入投票标题';
    return;
  }
  
  if (options.value.some(opt => !opt.content.trim())) {
    errorMessage.value = '所有选项内容不能为空';
    return;
  }
  
  try {
    // 构造投票数据
    const pollData = {
      title: title.value,
      description: description.value,
      options: options.value.map(opt => ({ content: opt.content })),
      startDate: formatDateTime(startDate.value),
      endDate: formatDateTime(endDate.value),
      isActive: true,
    };
    
    console.log('发送数据:', pollData); // 调试用
    
    // 调用API创建投票
    const response = await fetch('http://localhost:8080/api/polls', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(pollData),
    });
    
    if (response.ok) {
      emit('created');
    } else {
      // 尝试解析错误响应
      try {
        const errorData = await response.json();
        errorMessage.value = errorData.message || '创建投票失败，请重试';
      } catch (e) {
        errorMessage.value = '创建投票失败，请重试';
      }
    }
  } catch (error) {
    console.error('创建投票出错:', error);
    errorMessage.value = '创建投票时发生错误';
  }
};
</script>

<template>
  <div class="create-poll">
    <h2>创建新投票</h2>
    
    <div v-if="errorMessage" class="error-message">
      {{ errorMessage }}
    </div>
    
    <form @submit.prevent="createPoll">
      <div class="form-group">
        <label for="title">投票标题 *</label>
        <input 
          id="title" 
          v-model="title" 
          type="text" 
          placeholder="输入投票标题" 
          required
        />
      </div>
      
      <div class="form-group">
        <label for="description">描述（可选）</label>
        <textarea 
          id="description" 
          v-model="description" 
          placeholder="输入投票描述"
          rows="3"
        ></textarea>
      </div>

      <div class="form-group">
        <label for="startDate">开始日期和时间</label>
        <input 
          id="startDate" 
          v-model="startDate" 
          type="datetime-local" 
          class="datetime-input"
        />
      </div>
      
      <div class="form-group">
        <label for="endDate">结束日期和时间（可选）</label>
        <input 
          id="endDate" 
          v-model="endDate" 
          type="datetime-local" 
          class="datetime-input"
        />
      </div>
      
      <div class="options-section">
        <h3>投票选项 *</h3>
        <div 
          v-for="(option, index) in options" 
          :key="index"
          class="option-item"
        >
          <input 
            v-model="option.content" 
            type="text" 
            :placeholder="`选项 ${index + 1}`"
            required
          />
          <button 
            type="button" 
            @click="removeOption(index)" 
            class="remove-btn"
          >
            删除
          </button>
        </div>
        
        <button 
          type="button" 
          @click="addOption" 
          class="add-option-btn"
        >
          添加选项
        </button>
      </div>
      
      <div class="actions">
        <button type="submit" class="submit-btn">创建投票</button>
      </div>
    </form>
  </div>
</template>

<style scoped>
.create-poll {
  padding: 20px;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: 500;
}

input, textarea {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.datetime-input {
  width: 100%;
}

.options-section {
  margin-top: 20px;
}

.option-item {
  display: flex;
  margin-bottom: 10px;
  gap: 10px;
}

.option-item input {
  flex-grow: 1;
}

.remove-btn {
  background-color: #ff4757;
  color: white;
}

.add-option-btn {
  margin-top: 10px;
  background-color: #1e90ff;
  color: white;
  width: 100%;
}

.actions {
  margin-top: 25px;
  text-align: center;
}

.submit-btn {
  background-color: #2ed573;
  color: white;
  padding: 10px 20px;
  font-size: 16px;
}

.error-message {
  background-color: #ff6b6b;
  color: white;
  padding: 10px;
  border-radius: 4px;
  margin-bottom: 15px;
}
</style>