<template>
  <div class="main-layout">
    <!-- 移动端菜单按钮 -->
    <div class="mobile-menu-btn" @click="toggleSidebar" v-if="isMobile">
      <el-icon><Menu /></el-icon>
    </div>
    
    <!-- 侧边栏 -->
    <div class="sidebar-container" :class="{ 'sidebar-hidden': isMobile && !sidebarVisible }">
      <Sidebar />
    </div>
    
    <!-- 移动端遮罩层 -->
    <div class="sidebar-overlay" v-if="isMobile && sidebarVisible" @click="toggleSidebar"></div>
    
    <div class="main-container">
      <div class="header-container">
        <HeaderBar />
      </div>
      <div class="content-container">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted } from 'vue'
import { Menu } from '@element-plus/icons-vue'
import Sidebar from './SideBar.vue';
import HeaderBar from './HeaderBar.vue';

export default {
  name: 'MainLayout',
  components: {
    Sidebar,
    HeaderBar,
    Menu
  },
  setup() {
    const isMobile = ref(false)
    const sidebarVisible = ref(false)

    const checkIsMobile = () => {
      isMobile.value = window.innerWidth <= 768
      if (!isMobile.value) {
        sidebarVisible.value = false
      }
    }

    const toggleSidebar = () => {
      sidebarVisible.value = !sidebarVisible.value
    }

    onMounted(() => {
      checkIsMobile()
      window.addEventListener('resize', checkIsMobile)
    })

    onUnmounted(() => {
      window.removeEventListener('resize', checkIsMobile)
    })

    return {
      isMobile,
      sidebarVisible,
      toggleSidebar
    }
  }
}
</script>

<style scoped>
.main-layout {
  display: flex;
  height: 100vh;
  width: 100%;
  overflow: hidden;
  position: relative;
}

.mobile-menu-btn {
  position: fixed;
  top: 15px;
  left: 15px;
  z-index: 1001;
  background: #409eff;
  color: white;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.2);
}

.sidebar-container {
  width: 240px;
  height: 100%;
  transition: transform 0.3s ease;
  z-index: 1000;
}

.sidebar-hidden {
  transform: translateX(-100%);
}

.sidebar-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 999;
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.header-container {
  height: 60px;
  flex-shrink: 0;
}

.content-container {
  flex: 1;
  overflow-y: auto;
  background-color: #f5f7fa;
  padding: 16px;
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .sidebar-container {
    position: fixed;
    top: 0;
    left: 0;
    height: 100vh;
    z-index: 1000;
    box-shadow: 2px 0 12px rgba(0, 0, 0, 0.2);
  }
  
  .main-container {
    width: 100%;
    margin-left: 0;
  }
  
  .content-container {
    padding: 12px 8px;
  }
  
  .header-container {
    padding-left: 50px; /* 为菜单按钮留出空间 */
  }
}

/* 手机端适配 */
@media screen and (max-width: 480px) {
  .content-container {
    padding: 8px 4px;
  }
  
  .mobile-menu-btn {
    width: 35px;
    height: 35px;
    top: 12px;
    left: 12px;
  }
}
</style>