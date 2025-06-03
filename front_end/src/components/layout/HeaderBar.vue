<template>
  <div class="header-bar">
    <div class="header-content">
      <div class="left-section">
        <el-autocomplete
          v-model="searchText"
          :fetch-suggestions="searchStocks"
          placeholder="搜索股票代码或名称"
          class="search-input"
          popper-class="stock-search-popper"
          @select="handleStockSelect"
          clearable
        >
          <template #prefix>
            <el-icon class="search-icon"><Search /></el-icon>
          </template>
          <template #default="{ item }">
            <div class="stock-search-item">
              <div class="stock-search-name">{{ item.name }}</div>
              <div class="stock-search-code">{{ item.code }}</div>
            </div>
          </template>
        </el-autocomplete>
      </div>
      <div class="right-section">
        <div class="datetime">
          <span>{{ currentDate }}</span>
          <span class="time">{{ currentTime }}</span>
        </div>
        <div class="notification">
          <el-badge :value="3" class="notification-badge">
            <el-icon size="20"><Bell /></el-icon>
          </el-badge>
        </div>
        <div class="user-info">
          <el-dropdown trigger="click">
            <div class="user-profile">
              <el-avatar size="small" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
              <div class="user-details">
                <span class="username">{{ currentUser }}</span>
                <span class="login-time">上次登录: {{ loginTime }}</span>
              </div>
              <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu class="apple-dropdown-menu">
                <el-dropdown-item @click="goToAccount">
                  <el-icon><User /></el-icon>个人资料
                </el-dropdown-item>
                <el-dropdown-item @click="goToProfile">
                  <el-icon><Setting /></el-icon>设置
                </el-dropdown-item>
                <el-dropdown-item v-if="isGuest" divided @click="goToLogin">
                  <el-icon><Key /></el-icon>登录
                </el-dropdown-item>
                <el-dropdown-item v-else divided @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>    </div>
      <!-- 股票详情弹窗 -->
    <stock-detail-dialog 
      :visible="stockDialogVisible" 
      @update:visible="stockDialogVisible = $event"
      :stock="selectedStock" 
      @close="stockDialogVisible = false"
    />
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted, nextTick, watch,computed } from 'vue';
import { Search, Bell, ArrowDown, User, Setting, SwitchButton,Key } from '@element-plus/icons-vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import StockDetailDialog from '@/components/stock/StockDetailDialog.vue';
import {authState,syncAuthState} from '@/store/authStore.js'; // 导入认证状态
import { getAllAvailableStocks, getStockRealtimeData } from '@/utils/stockDataService';

// 导入用户管理器
import { logoutUser } from '@/utils/userManager';

export default {
  name: 'HeaderBar',
  components: {
    Search,
    Bell,
    ArrowDown,
    User,
    Setting,
    SwitchButton,
    Key,
    StockDetailDialog
  },
  setup() {
    const router = useRouter();
    const searchText = ref('');
    const currentDate = ref('');
    const currentTime = ref('');
    const loginTime = ref('2025-04-15 08:30');
    const currentUser = computed(() => authState.username || '访客' );
    const isGuest = computed(() => currentUser.value === '访客');
    let timer = null;

    // 股票详情弹窗相关
    const stockDialogVisible = ref(false);
    const selectedStock = ref({});

    // 从 stockDataService 获取完整的股票数据
    const stockList = ref([]);
    
    // 初始化股票数据
    const initializeStockList = () => {
      const allStocks = getAllAvailableStocks();
      stockList.value = allStocks.map(stock => ({
        name: stock.name,
        code: stock.code,
        sector: stock.sector,
        market: stock.market,
        change: 0 // 默认涨跌幅
      }));
      console.log('初始化股票列表:', stockList.value);
    };

    // 搜索股票
    const searchStocks = (queryString, callback) => {
      if (queryString) {
        // 过滤股票名称或代码包含搜索词的股票
        const results = stockList.value.filter(
          stock => 
            stock.name.toLowerCase().includes(queryString.toLowerCase()) || 
            stock.code.includes(queryString)
        );
        // 确保每个结果都是完整的股票对象
        console.log('搜索关键词:', queryString, '搜索结果:', results);
        callback(results);
      } else {
        callback([]);
      }
    };

    // 选择股票
    const handleStockSelect = (stock) => {
      console.log('从搜索框选择的股票:', stock);
      // 清空搜索框
      searchText.value = ''; 
      
      if (!stock || !stock.code) {
        console.error('选择的股票数据无效');
        ElMessage.error('选择的股票数据无效');
        return;
      }
      
      try {
        // 获取股票的详细实时数据，与排行榜保持一致
        const detailData = getStockRealtimeData(stock.code);
        
        selectedStock.value = {
          ...stock,
          ...detailData,
          // 补充详情页需要的额外信息
          pe: generatePERatio(stock.code),
          pb: generatePBRatio(stock.code),
          prevClosePrice: (parseFloat(detailData.currentPrice) - parseFloat(detailData.changeAmount)).toFixed(2)
        };
        
        console.log('准备使用的股票数据:', selectedStock.value);
        
        nextTick(() => {
          console.log('nextTick - 设置弹窗显示');
          stockDialogVisible.value = true;
        });
      } catch (error) {
        console.error('获取股票详情失败:', error);
        ElMessage.error('获取股票详情失败，请稍后再试');
      }
    };

    // 生成市盈率（基于股票代码的稳定值）
    const generatePERatio = (stockCode) => {
      const hash = hashString(stockCode + 'pe');
      return ((hash % 300 + 50) / 10).toFixed(1); // 5.0-35.0
    };

    // 生成市净率（基于股票代码的稳定值）
    const generatePBRatio = (stockCode) => {
      const hash = hashString(stockCode + 'pb');
      return ((hash % 80 + 10) / 10).toFixed(1); // 1.0-9.0
    };

    // 简单的字符串哈希函数
    const hashString = (str) => {
      let hash = 0;
      for (let i = 0; i < str.length; i++) {
        const char = str.charCodeAt(i);
        hash = ((hash << 5) - hash) + char;
        hash = hash & hash; // 转换为32位整数
      }
      return Math.abs(hash);
    };

    const updateDateTime = () => {
      const now = new Date();
      const options = { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' };
      currentDate.value = now.toLocaleDateString('zh-CN', options);
      currentTime.value = now.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
    };

    // 导航到账户页面
    const goToAccount = () => {
      router.push('/account');
    };

    // 导航到个人信息页面
    const goToProfile = () => {
      router.push('/profile');
    };

    // 退出登录
    const handleLogout = () => {
      // 使用用户管理器处理登出
      logoutUser();
      router.push('/Dashboard');
      ElMessage.success('已成功退出登录');
    };

    // 导航到登录页面
    const goToLogin = () => {
      router.push('/login');
    };

    // 监听弹窗状态变化，用于调试
    const watchStockDialogVisible = (newVal) => {
      console.log('stockDialogVisible 变化为:', newVal);
    };
    
    // 监听对象
    watch(stockDialogVisible, watchStockDialogVisible);
    
    // 更新当前用户名
    // const updateCurrentUser = () => {
    //   const username = sessionStorage.getItem('loggedInUserDemo')
    //   currentUser.value = username || '访客'
    // }

    onMounted(() => {
      syncAuthState() // 同步认证状态
      updateDateTime()
      // updateCurrentUser()
      timer = setInterval(updateDateTime, 60000) // 每分钟更新一次
      initializeStockList()
    })

    onUnmounted(() => {
      if (timer) {
        clearInterval(timer);
      }
    });

    return {
      searchText,
      currentDate,
      currentTime,
      loginTime,
      currentUser,
      isGuest,
      goToAccount,
      goToProfile,
      handleLogout,
      goToLogin,
      searchStocks,
      handleStockSelect,
      stockDialogVisible,
      selectedStock
    };
  }
};
</script>

<style scoped>
.header-bar {
  background-color: rgba(22, 22, 23, 0.8);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  box-shadow: 0 1px 0 rgba(255, 255, 255, 0.08);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 48px; /* 增加高度，从44px到48px，让顶栏更宽松 */
  padding: 0 24px; /* 增加左右内边距，从16px到24px */
  max-width: 1280px;
  margin: 0 auto;
}

.left-section {
  flex: 1;
  display: flex;
  align-items: center;
  padding-right: 20px; /* 增加右侧内边距，使左侧与右侧部分有更好的分隔 */
}

.search-input {
  max-width: 180px; /* 维持搜索框宽度为180px */
  margin-left: 16px; /* 增加左侧边距，从8px到16px */
}

.search-input :deep(.el-input__wrapper) {
  background-color: rgba(255, 255, 255, 0.1);
  border: none;
  box-shadow: none;
  border-radius: 8px;
  padding: 0 12px;
  transition: background-color 0.3s;
}

.search-input :deep(.el-input__wrapper:hover) {
  background-color: rgba(255, 255, 255, 0.15);
}

.search-input :deep(.el-input__wrapper.is-focus) {
  background-color: rgba(255, 255, 255, 0.2);
}

.search-input :deep(.el-input__inner) {
  color: #f5f5f7;
  font-size: 14px;
}

.search-input :deep(.el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.65);
}

.search-icon {
  color: rgba(255, 255, 255, 0.65);
}

:deep(.stock-search-popper) {
  border-radius: 10px;
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.2);
  border: none;
  width: 180px; /* 将下拉框宽度从220px进一步减少到180px，与搜索框宽度一致 */
  background-color: #FFFFFF;
}

:deep(.stock-search-popper .el-popper__arrow) {
  display: none;
}

:deep(.stock-search-popper .el-autocomplete-suggestion__list) {
  padding: 8px 0;
}

.stock-search-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 16px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.stock-search-item:hover {
  background-color: #f5f7fa;
}

.stock-search-name {
  font-weight: 500;
  color: #303133;
}

.stock-search-code {
  color: #909399;
  font-size: 0.9em;
}

.right-section {
  display: flex;
  align-items: center;
  gap: 36px; /* 增加元素之间的间距，从24px增加到36px */
}

.datetime {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  font-size: 14px;
  color: #f5f5f7;
}

.time {
  font-weight: 400;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
}

.notification-badge {
  cursor: pointer;
  transition: transform 0.2s;
  color: #f5f5f7;
}

.notification-badge:hover {
  transform: scale(1.05);
}

:deep(.el-badge__content) {
  background-color: #0071e3;
  border: none;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 12px; /* 增加用户头像与文本之间的间距，从10px到12px */
  cursor: pointer;
  padding: 6px 12px; /* 增加内边距，从4px 8px到6px 12px */
  border-radius: 20px;
  transition: background-color 0.2s;
}

.user-profile:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.user-details {
  display: flex;
  flex-direction: column;
  margin-right: 8px; /* 增加右侧边距，从5px到8px */
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: #f5f5f7;
}

.login-time {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.65);
}

.dropdown-icon {
  color: rgba(255, 255, 255, 0.65);
  transition: transform 0.3s;
}

:deep(.el-popper) {
  min-width: 160px;
  border-radius: 10px;
  border: none;
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.2);
}

:deep(.apple-dropdown-menu) {
  background-color: #FFFFFF;
  padding: 8px 0;
  border-radius: 10px;
}

:deep(.el-dropdown-menu__item) {
  color: #303133 !important; /* 修复下拉菜单文字颜色为深色 */
  display: flex;
  align-items: center;
  gap: 12px; /* 增加图标与文本之间的间距，从8px到12px */
  padding: 12px 20px; /* 增加内边距，从10px 16px到12px 20px */
  font-size: 14px;
  transition: background-color 0.2s;
}

:deep(.el-dropdown-menu__item:hover) {
  background-color: #f5f7fa;
  color: #409EFF !important; /* 鼠标悬停时为Element Plus主题蓝色 */
}

:deep(.el-dropdown-menu__item i) {
  color: #606266;
  margin-right: 4px;
}

:deep(.el-divider--horizontal) {
  margin: 4px 0;
  background-color: #EBEEF5;
}

@media (max-width: 768px) {
  .header-content {
    padding: 0 16px;
    height: 60px;
  }
  
  .left-section {
    flex: 1;
    max-width: 200px;
  }
  
  .search-input {
    width: 100% !important;
    max-width: 180px;
  }
  
  .datetime {
    display: none;
  }
  
  .user-details {
    display: none;
  }
  
  .user-profile {
    gap: 4px;
  }
  
  .right-section {
    gap: 16px;
  }
  
  .notification {
    display: flex;
    align-items: center;
  }
}

/* 手机端进一步优化 */
@media (max-width: 480px) {
  .header-content {
    padding: 0 12px;
  }
  
  .left-section {
    max-width: 120px;
  }
  
  .search-input {
    max-width: 120px;
  }
  
  .search-input :deep(.el-input__inner) {
    font-size: 14px;
    padding: 8px 12px;
  }
  
  .right-section {
    gap: 12px;
  }
  
  .user-profile {
    min-width: auto;
  }
  
  .dropdown-icon {
    display: none;
  }
}
</style>