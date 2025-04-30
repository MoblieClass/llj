<script setup>
import { ref } from 'vue';

const props = defineProps({
  poll: {
    type: Object,
    required: true
  }
});

const emit = defineEmits(['updated', 'back']);

const title = ref(props.poll.title);
const description = ref(props.poll.description || '');
const options = ref(props.poll.options.map(opt => ({ ...opt })));
const endDate = ref(props.poll.endDate ? new Date(props.poll.endDate).toISOString().split('T')[0] : '');
const isActive = ref(props.poll.isActive);
const errorMessage = ref('');

const addOption = () => {
  options.value.push({ content: '', votes: 0 });
};
const formatDateTime = (dateTimeString) => {
  if (!dateTimeString) return null;
  // 将本地日期时间字符串转换为ISO 8601格式
  const date = new Date(dateTimeString);
  return date.toISOString();
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

const updatePoll = async () => {
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
    // 构造更新后的投票数据
    const updatedPoll = {
      title: title.value,
      description: description.value,
      endDate: formatDateTime(endDate.value),
      isActive: isActive.value,
      options: options.value.map(opt => ({
        id: opt.id,
        content: opt.content,
        votes: opt.votes
      }))
    };

    console.log('发送更新数据:', updatedPoll); // 调试用
    
    // 调用API更新投票
    // 这里应该是实际的API调用
    const response = await fetch(`http://localhost:8080/api/polls/${props.poll.id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(updatedPoll),
    });
    
    if (response.ok) {
      emit('updated');
    } else {
      errorMessage.value = '更新投票失败，请重试';
    }
  } catch (error) {
    console.error('更新投票出错:', error);
    errorMessage.value = '更新投票时发生错误';
  }
};

const cancel = () => {
  emit('back');
};
</script>

<template>
  <div class="edit-poll">
    <h2>编辑投票</h2>
    
    <div v-if="errorMessage" class="error-message">
      {{ errorMessage }}
    </div>
    
    <form @submit.prevent="updatePoll">
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
        <label for="endDate">结束日期和时间（可选）</label>
        <input 
          id="endDate" 
          v-model="endDate" 
          type="datetime-local" 
          class="datetime-input"
        />
      </div>
      
      <div class="form-group">
        <label class="checkbox-label">
          <input type="checkbox" v-model="isActive" />
          投票活跃状态
        </label>
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
          <div class="option-votes">
            投票数: {{ option.votes }}
          </div>
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
        <button type="button" @click="cancel" class="cancel-btn">取消</button>
        <button type="submit" class="submit-btn">保存修改</button>
      </div>
    </form>
  </div>
</template>

<style scoped>
.edit-poll {
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

.checkbox-label {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.checkbox-label input {
  width: auto;
  margin-right: 8px;
}

.options-section {
  margin-top: 20px;
}

.option-item {
  display: flex;
  margin-bottom: 10px;
  gap: 10px;
  align-items: center;
}

.option-item input {
  flex-grow: 1;
}

.option-votes {
  min-width: 80px;
  font-size: 14px;
  color: #666;
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
  display: flex;
  justify-content: space-between;
}

.cancel-btn {
  background-color: #f0f0f0;
  color: #333;
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