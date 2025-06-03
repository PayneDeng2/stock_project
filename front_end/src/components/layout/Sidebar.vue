<template>
  <div class="sidebar">
    <div class="logo-container">
      <h2 class="logo">股票系统</h2>
    </div>
    <el-menu
      :default-active="activeMenu"
      class="sidebar-menu"
      background-color="#2c3e50"
      text-color="#ffffff"
      active-text-color="#42b983"
      router
    >
      <el-menu-item index="/dashboard">
        <el-icon><HomeFilled /></el-icon>
        <span>系统首页</span>
      </el-menu-item>
      <el-menu-item index="/trading">
        <el-icon><Sell /></el-icon>
        <span>交易</span>
      </el-menu-item>
      <el-menu-item index="/orderbook">
        <el-icon><List /></el-icon>
        <span>订单簿</span>
      </el-menu-item>
      <el-menu-item index="/account">
        <el-icon><User /></el-icon>
        <span>我的账户</span>
      </el-menu-item>
      <el-menu-item index="/profile">
        <el-icon><Setting /></el-icon>
        <span>个人信息</span>
      </el-menu-item>
    </el-menu>
  </div>
</template>

<script>
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import { HomeFilled, Sell, List, User, Setting } from '@element-plus/icons-vue';

export default {
  name: 'SidebarMenu',
  components: {
    HomeFilled,
    Sell,
    List,
    User,
    Setting
  },
  setup() {
    const route = useRoute();
    
    const activeMenu = computed(() => {
      return route.path;
    });

    return {
      activeMenu
    };
  }
}
</script>

<style scoped>
.sidebar {
  height: 100%;
  background-color: #2c3e50;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.logo-container {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 10px 0;
}

.logo {
  color: white;
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.sidebar-menu {
  border-right: none;
}

.el-menu-item {
  display: flex;
  align-items: center;
}

.el-menu-item .el-icon {
  margin-right: 8px;
}
</style>