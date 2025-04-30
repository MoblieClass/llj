<script setup>
import PollSystem from './components/poll/PollSystem.vue'
import { onMounted } from 'vue';

// 初始化粒子背景
onMounted(() => {
  initParticleBackground();
});

// 粒子背景效果
const initParticleBackground = () => {
  const canvas = document.getElementById('particles-background');
  if (!canvas) return;
  
  const ctx = canvas.getContext('2d');
  canvas.width = window.innerWidth;
  canvas.height = window.innerHeight;
  
  let particles = [];
  
  class Particle {
    constructor() {
      this.x = Math.random() * canvas.width;
      this.y = Math.random() * canvas.height;
      this.size = Math.random() * 5 + 1;
      this.speedX = Math.random() * 3 - 1.5;
      this.speedY = Math.random() * 3 - 1.5;
      this.color = `hsla(${Math.random() * 60 + 200}, 80%, 60%, ${Math.random() * 0.3 + 0.1})`;
    }
    
    update() {
      this.x += this.speedX;
      this.y += this.speedY;
      
      if (this.x > canvas.width || this.x < 0) this.speedX = -this.speedX;
      if (this.y > canvas.height || this.y < 0) this.speedY = -this.speedY;
    }
    
    draw() {
      ctx.fillStyle = this.color;
      ctx.beginPath();
      ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2);
      ctx.fill();
    }
  }
  
  const init = () => {
    particles = [];
    for (let i = 0; i < 40; i++) {
      particles.push(new Particle());
    }
  };
  
  const animate = () => {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    particles.forEach(particle => {
      particle.update();
      particle.draw();
    });
    
    // 绘制连接线
    connectParticles();
    
    requestAnimationFrame(animate);
  };
  
  const connectParticles = () => {
    for (let i = 0; i < particles.length; i++) {
      for (let j = i; j < particles.length; j++) {
        const dx = particles[i].x - particles[j].x;
        const dy = particles[i].y - particles[j].y;
        const distance = Math.sqrt(dx * dx + dy * dy);
        
        if (distance < 120) {
          ctx.beginPath();
          ctx.strokeStyle = `rgba(148, 187, 233, ${0.2 - distance/600})`;
          ctx.lineWidth = 0.4;
          ctx.moveTo(particles[i].x, particles[i].y);
          ctx.lineTo(particles[j].x, particles[j].y);
          ctx.stroke();
          ctx.closePath();
        }
      }
    }
  };
  
  // 响应窗口调整
  window.addEventListener('resize', () => {
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
    init();
  });
  
  init();
  animate();
};
</script>

<template>
  <div class="app-container">
    <!-- 粒子背景 -->
    <canvas id="particles-background"></canvas>
    
    <!-- 光效背景 -->
    <div class="light-effects">
      <div class="light-orb light-orb-1"></div>
      <div class="light-orb light-orb-2"></div>
      <div class="light-orb light-orb-3"></div>
    </div>
    
    <div class="content-wrapper">
      <header class="app-header">
        <div class="logo-container">
          <div class="logo-icon">
            <span class="material-icons">how_to_vote</span>
          </div>
          <h1>Vote<span class="accent">Pulse</span></h1>
        </div>
        <p class="app-description">创建精彩投票 · 收集真实反馈 · 可视化分析结果</p>
      </header>
      
      <main>
        <PollSystem />
      </main>
      
      <footer class="app-footer">
        <div class="footer-content">
          <p>&copy; 2025 VotePulse - 让每个声音都被听见</p>
          <div class="footer-links">
            <a href="#">关于我们</a>
            <a href="#">隐私政策</a>
            <a href="#">使用条款</a>
          </div>
        </div>
      </footer>
    </div>
  </div>
</template>

<style>
@import url('https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap');
@import url('https://fonts.googleapis.com/icon?family=Material+Icons');

/* 全局样式 */
:root {
  --primary-color: #4361ee;
  --secondary-color: #3a0ca3;
  --accent-color: #4cc9f0;
  --text-color: #2b2d42;
  --bg-color: #f8f9fa;
  --card-bg: #ffffff;
  --border-color: #e5e7eb;
  --success-color: #2ecc71;
  --warning-color: #f39c12;
  --error-color: #e74c3c;
  --header-height: 70px;
}

@media (prefers-color-scheme: dark) {
  :root {
    --primary-color: #4361ee;
    --secondary-color: #7209b7;
    --accent-color: #4cc9f0;
    --text-color: #e2e2e2;
    --bg-color: #121212;
    --card-bg: #1e1e1e;
    --border-color: #2c2c2c;
  }
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  color: var(--text-color);
  background-color: var(--bg-color);
  font-family: 'Poppins', sans-serif;
  margin: 0;
  min-height: 100vh;
  overflow-x: hidden;
}

#app {
  width: 100%;
  max-width: none;
  padding: 0;
  margin: 0;
}

.app-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  position: relative;
  overflow: hidden;
}

/* 粒子背景 */
#particles-background {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
}

/* 光效背景 */
.light-effects {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
  overflow: hidden;
}

.light-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
}

.light-orb-1 {
  top: -10%;
  left: -10%;
  width: 60%;
  height: 60%;
  background: radial-gradient(circle, rgba(67, 97, 238, 0.3) 0%, rgba(67, 97, 238, 0) 70%);
  animation: float-1 15s infinite ease-in-out;
}

.light-orb-2 {
  bottom: -20%;
  right: -10%;
  width: 70%;
  height: 70%;
  background: radial-gradient(circle, rgba(76, 201, 240, 0.3) 0%, rgba(76, 201, 240, 0) 70%);
  animation: float-2 18s infinite ease-in-out;
}

.light-orb-3 {
  top: 40%;
  left: 30%;
  width: 40%;
  height: 40%;
  background: radial-gradient(circle, rgba(114, 9, 183, 0.2) 0%, rgba(114, 9, 183, 0) 70%);
  animation: float-3 12s infinite ease-in-out;
}

@keyframes float-1 {
  0% { transform: translate(0, 0); }
  50% { transform: translate(5%, 10%); }
  100% { transform: translate(0, 0); }
}

@keyframes float-2 {
  0% { transform: translate(0, 0); }
  50% { transform: translate(-7%, -5%); }
  100% { transform: translate(0, 0); }
}

@keyframes float-3 {
  0% { transform: translate(0, 0); }
  50% { transform: translate(7%, -7%); }
  100% { transform: translate(0, 0); }
}

.content-wrapper {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  backdrop-filter: blur(5px);
}

/* 玻璃拟态效果 */
.app-header {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  padding: 1.5rem 2rem;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.dark .app-header {
  background: rgba(20, 20, 20, 0.8);
  border-bottom: 1px solid rgba(50, 50, 50, 0.2);
}

.logo-container {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.logo-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  border-radius: 12px;
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  color: white;
  font-size: 24px;
  box-shadow: 0 4px 15px rgba(67, 97, 238, 0.3);
}

.app-header h1 {
  margin: 0;
  font-size: 2.4rem;
  font-weight: 700;
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: -1px;
}

.accent {
  color: var(--accent-color);
  -webkit-text-fill-color: var(--accent-color);
}

.app-description {
  margin-top: 0.25rem;
  font-size: 1.1rem;
  font-weight: 300;
  opacity: 0.8;
}

main {
  flex: 1;
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
}

/* 卡片效果 */
.card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 2rem;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  border: 1px solid rgba(255, 255, 255, 0.2);
  overflow: hidden;
}

.card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.1);
}

.dark .card {
  background: rgba(30, 30, 30, 0.9);
  border: 1px solid rgba(60, 60, 60, 0.2);
}

/* 按钮样式 */

button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

button:active {
  transform: translateY(0);
}

/* 输入框样式 */
input, textarea, select {
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(0, 0, 0, 0.1);
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 16px;
  transition: all 0.3s ease;
  font-family: 'Poppins', sans-serif;
}

input:focus, textarea:focus, select:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(67, 97, 238, 0.2);
}

.dark input, .dark textarea, .dark select {
  background: rgba(40, 40, 40, 0.9);
  color: var(--text-color);
  border-color: rgba(255, 255, 255, 0.1);
}

/* 页脚样式 */
.app-footer {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-top: 1px solid rgba(255, 255, 255, 0.2);
  padding: 1.5rem 2rem;
  text-align: center;
  font-size: 0.9rem;
  color: var(--text-color);
  opacity: 0.8;
}

.dark .app-footer {
  background: rgba(20, 20, 20, 0.9);
  border-top: 1px solid rgba(50, 50, 50, 0.2);
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.footer-links {
  display: flex;
  gap: 20px;
}

.footer-links a {
  color: var(--text-color);
  text-decoration: none;
  transition: color 0.2s ease;
}

.footer-links a:hover {
  color: var(--primary-color);
}

/* 响应式布局 */
@media (max-width: 768px) {
  .app-header {
    padding: 1rem;
  }

  .app-header h1 {
    font-size: 1.8rem;
  }

  .app-description {
    font-size: 0.9rem;
  }

  main {
    padding: 1rem;
  }
  
  .footer-content {
    flex-direction: column;
    gap: 10px;
  }
  
  button {
    padding: 10px 20px;
  }
}

/* 动画效果 */
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.fade-in {
  animation: fadeIn 0.6s ease forwards;
}

/* 模态框动画 */
@keyframes modalIn {
  from { opacity: 0; transform: scale(0.95); }
  to { opacity: 1; transform: scale(1); }
}

.modal {
  animation: modalIn 0.3s ease forwards;
}

/* 进度条动画 */
@keyframes progressFill {
  from { width: 0; }
  to { width: 100%; }
}

/* 脉冲效果 */
@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.05); }
  100% { transform: scale(1); }
}

.pulse {
  animation: pulse 2s infinite ease-in-out;
}
</style>