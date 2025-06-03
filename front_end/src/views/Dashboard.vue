<template>
  <div class="dashboard">
    <!-- 顶部导航标签 -->
    <div class="nav-tabs">
      <div class="tab-item active">
        <el-icon><Promotion /></el-icon> A股
      </div>
    </div>

    <!-- 指标卡片区域 -->
    <div class="metrics-panel">
      <div class="metric-card" style="background-color: #8a9a9e;">
        <div class="card-header">涨跌停对比</div>
        <div class="metric-content">
          <div class="metric-value">28 : 45</div>
        </div>
      </div>
      
      <div class="metric-card" style="background-color: #b19795;">
        <div class="card-header">昨日涨停表现</div>
        <div class="metric-content">
          <div class="metric-value">0.8%</div>
        </div>
      </div>
      
      <div class="metric-card" style="background-color: #d3c0ad;">
        <div class="card-header">大小盘对比</div>
        <div class="metric-content">
          <div class="metric-value">-0.9%~-1.4%</div>
          <div class="sub-text">今日走势</div>
        </div>
      </div>
    </div>    <!-- 股票排行榜 -->
    <div class="stock-ranking-section">
      <div class="section-header">
        <h2>股票排行</h2>
      </div>

      <!-- 自选股区域 -->
      <div class="watchlist-section" v-if="watchlist.length > 0">
        <div class="section-header">
          <h2>我的自选股</h2>
          <el-button size="small" @click="refreshWatchlist">
            <el-icon><Refresh /></el-icon>刷新
          </el-button>
        </div>
        <div class="watchlist-card">
          <div class="stock-list">            <div v-for="stock in watchlist" :key="stock.code" class="stock-item watchlist-item" @click="showStockDetail(stock)">
              <div class="stock-info">
                <div class="stock-name">{{ stock.name }}</div>
                <div class="stock-code">{{ stock.code }}</div>
                <div class="stock-sector" v-if="stock.sector">{{ stock.sector }}</div>
              </div>              <div class="stock-price-info">
                <div class="current-price">
                  <span class="price-label">现价:</span>
                  <span class="price-value">¥{{ stock.currentPrice || '--' }}</span>
                </div>
                <div v-if="stock.change !== undefined" :class="{'stock-up': stock.change > 0, 'stock-down': stock.change < 0}" class="stock-change">
                  <span class="change-label">涨跌:</span>
                  <span class="change-value">{{ stock.change > 0 ? '+' : '' }}{{ stock.change }}%</span>
                </div>
                <div class="stock-volume" v-if="stock.turnover">
                  <span class="volume-label">成交:</span>
                  <span class="volume-value">{{ stock.turnover }}</span>
                </div>
              </div>
              <div class="stock-actions">
                <el-button size="small" type="text" @click.stop="removeFromWatchlistHandler(stock.code)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <el-row :gutter="20" class="ranking-container">        <!-- 涨幅榜 -->
        <el-col :span="8">
          <div class="ranking-card">
            <div class="ranking-title">涨幅榜</div>
            <div class="stock-list">
              <div v-for="(stock, index) in topGainers" :key="index" class="stock-item" @click="showStockDetail(stock)">
                <div class="stock-info">
                  <div class="stock-name">{{ stock.name }}</div>
                  <div class="stock-code">{{ stock.code }}</div>
                </div>
                <div class="stock-price-data">
                  <div class="current-price">¥{{ stock.currentPrice }}</div>
                  <div class="stock-change stock-up">+{{ stock.change }}%</div>
                </div>
              </div>
            </div>
          </div>
        </el-col>

        <!-- 跌幅榜 -->
        <el-col :span="8">
          <div class="ranking-card">
            <div class="ranking-title">跌幅榜</div>
            <div class="stock-list">
              <div v-for="(stock, index) in topLosers" :key="index" class="stock-item" @click="showStockDetail(stock)">
                <div class="stock-info">
                  <div class="stock-name">{{ stock.name }}</div>
                  <div class="stock-code">{{ stock.code }}</div>
                </div>
                <div class="stock-price-data">
                  <div class="current-price">¥{{ stock.currentPrice }}</div>
                  <div class="stock-change stock-down">{{ stock.change }}%</div>
                </div>
              </div>
            </div>
          </div>
        </el-col>

        <!-- 成交额榜 -->
        <el-col :span="8">
          <div class="ranking-card">
            <div class="ranking-title">成交额榜</div>
            <div class="stock-list">
              <div v-for="(stock, index) in topVolume" :key="index" class="stock-item" @click="showStockDetail(stock)">
                <div class="stock-info">
                  <div class="stock-name">{{ stock.name }}</div>
                  <div class="stock-code">{{ stock.code }}</div>
                </div>
                <div class="stock-price-data">
                  <div class="current-price">¥{{ stock.currentPrice }}</div>
                  <div class="stock-volume">{{ stock.volume }}</div>
                </div>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 最近收到的消息 -->
      <div class="messages-section">
        <div class="section-header">
          <h2>已推送消息</h2>
        </div>
        <div class="message-list">
          <div v-for="(message, index) in messages" :key="index" class="message-item">
            <el-icon color="#ff6b6a"><Bell /></el-icon>
            <div class="message-text">{{ message }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 股票详情弹窗 -->
    <stock-detail-dialog 
      :visible="stockDialogVisible" 
      @update:visible="stockDialogVisible = $event"
      :stock="selectedStock" 
    />
  </div>
</template>

<script>
import { ref, onMounted, onBeforeUnmount } from 'vue';
import { Bell, Promotion, Refresh, Delete } from '@element-plus/icons-vue';
import StockDetailDialog from '@/components/stock/StockDetailDialog.vue';
import { ElMessage } from 'element-plus';
import { getWatchlist, removeFromWatchlist } from '@/utils/stockManager';
import { updateWatchlistPrices, getStockRealtimeData, generateStockRankingData } from '@/utils/stockDataService';

export default {
  name: 'DashboardView',
  components: {
    Bell,
    Promotion,
    Refresh,
    Delete,
    StockDetailDialog
  },  setup() {
    const watchlist = ref([]);
    
    // 排行榜数据
    const topGainers = ref([]);
    const topLosers = ref([]);
    const topVolume = ref([]);
      const messages = ref([
      '【价格提醒】贵州茅台(600519)达到设定价格1600元',
      '【市场资讯】沪深两市成交额突破万亿，市场活跃度提升',
      '【板块异动】银行板块集体上涨，建议关注龙头股票',
      '【财报提醒】中国平安(601318)将于明日发布季度财报',
      '【技术信号】上证指数突破重要阻力位，或迎来反弹行情'
    ]);// 股票详情弹窗相关
    const stockDialogVisible = ref(false);
    const selectedStock = ref({});    // 组件挂载时加载自选股
    onMounted(() => {
      loadWatchlist();
      loadRankingData();
      
      // 设置定时刷新（每30秒）
      const refreshInterval = setInterval(() => {
        loadWatchlist();
        loadRankingData();
      }, 30000);
      
      // 组件卸载时清除定时器
      onBeforeUnmount(() => {
        if (refreshInterval) {
          clearInterval(refreshInterval);
        }
      });
    });    // 加载自选股列表
    const loadWatchlist = () => {
      const rawWatchlist = getWatchlist();
      // 使用稳定的价格数据服务更新价格信息
      watchlist.value = updateWatchlistPrices(rawWatchlist);
    };

    // 加载排行榜数据
    const loadRankingData = () => {
      const rankingData = generateStockRankingData();
      topGainers.value = rankingData.topGainers;
      topLosers.value = rankingData.topLosers;
      topVolume.value = rankingData.topVolume;
    };    // 刷新自选股
    const refreshWatchlist = () => {
      loadWatchlist();
      loadRankingData();
      ElMessage.success('数据已刷新');
    };

    // 从自选股中移除
    const removeFromWatchlistHandler = (stockCode) => {
      const success = removeFromWatchlist(stockCode);
      if (success) {
        loadWatchlist();
        ElMessage.success('已移出自选股');
      } else {
        ElMessage.error('移出失败');
      }
    };    // 显示股票详情弹窗
    const showStockDetail = async (stock) => {
      try {
        // 获取股票的详细实时数据
        const detailData = getStockRealtimeData(stock.code);
        
        selectedStock.value = {
          ...stock,
          ...detailData,
          // 补充一些详情页需要的额外信息
          pe: generatePERatio(stock.code),
          pb: generatePBRatio(stock.code),
          prevClosePrice: (parseFloat(detailData.currentPrice) - parseFloat(detailData.changeAmount)).toFixed(2)
        };
        
        stockDialogVisible.value = true;
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
      return ((hash % 50 + 10) / 10).toFixed(1); // 1.0-6.0
    };

    // 简单的字符串哈希函数（与stockDataService中保持一致）
    const hashString = (str) => {
      let hash = 0;
      for (let i = 0; i < str.length; i++) {
        const char = str.charCodeAt(i);
        hash = ((hash << 5) - hash) + char;
        hash = hash & hash;
      }
      return Math.abs(hash);
    };return {
      watchlist,
      topGainers,
      topLosers,
      topVolume,
      messages,
      stockDialogVisible,
      selectedStock,
      showStockDetail,
      loadWatchlist,
      refreshWatchlist,
      removeFromWatchlistHandler
    };
  }
};
</script>

<style scoped>
.dashboard {
  padding: 28px;
  background-color: #f9fafc;
  min-height: 100%;
}

.nav-tabs {
  display: flex;
  margin-bottom: 24px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  padding-bottom: 6px;
}

.tab-item {
  padding: 12px 18px;
  margin-right: 28px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 10px;
  transition: all 0.2s ease;
  border-radius: 8px 8px 0 0;
}

.tab-item:hover {
  background-color: rgba(64, 158, 255, 0.05);
}

.tab-item.active {
  color: #409eff;
  border-bottom: 3px solid #409eff;
}

/* 指标卡片样式 */
.metrics-panel {
  display: flex;
  gap: 24px;
  margin-bottom: 32px;
}

.metric-card {
  flex: 1;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.04);
  color: rgba(255, 255, 255, 0.95);
  padding: 0;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  position: relative;
  z-index: 1;
}

.metric-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(255, 255, 255, 0.1);
  opacity: 0;
  z-index: -1;
  transition: opacity 0.3s ease;
}

.metric-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 16px 32px rgba(0, 0, 0, 0.1);
}

.metric-card:hover::before {
  opacity: 1;
}

.card-header {
  padding: 18px;
  font-weight: 600;
  font-size: 17px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
  letter-spacing: 0.4px;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.metric-content {
  padding: 28px 18px;
  text-align: center;
}

.metric-value {
  font-size: 32px;
  font-weight: 700;
  line-height: 1.2;
  letter-spacing: 0.5px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.15);
}

.sub-text {
  font-size: 14px;
  margin-top: 10px;
  opacity: 0.9;
  font-weight: 500;
}

/* 股票排行榜样式 */
.stock-ranking-section {
  margin-top: 32px;
}

/* 自选股区域样式 */
.watchlist-section {
  margin-bottom: 32px;
}

.watchlist-card {
  background-color: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
}

.watchlist-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  transform: translateY(-3px);
}

.watchlist-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 18px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  transition: background-color 0.2s ease;
  cursor: pointer;
}

.watchlist-item:hover {
  background-color: rgba(64, 158, 255, 0.05);
}

.watchlist-item:last-child {
  border-bottom: none;
}

.stock-price-info {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  margin-right: 12px;
}

.current-price {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 2px;
}

.price-label, .change-label, .volume-label {
  font-size: 12px;
  color: #909399;
  font-weight: 500;
  margin-right: 4px;
}

.price-value {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.change-value {
  font-weight: 700;
  font-size: 14px;
}

.volume-value {
  font-size: 12px;
  font-weight: 600;
  color: #606266;
}

.stock-actions {
  display: flex;
  align-items: center;
}

.stock-actions .el-button {
  color: #909399;
  font-size: 16px;
}

.stock-actions .el-button:hover {
  color: #f56c6c;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 22px;
  padding-bottom: 8px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
}

.section-header h2 {
  font-size: 22px;
  margin: 0;
  font-weight: 700;
  color: #303133;
  letter-spacing: 0.3px;
}

.compare-btn {
  font-weight: 600;
  border-radius: 8px;
  padding: 10px 16px;
  transition: all 0.2s ease;
}

.compare-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

.ranking-container {
  margin-bottom: 36px;
}

.ranking-card {
  background-color: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
  height: 100%;
  transition: all 0.3s ease;
}

.ranking-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  transform: translateY(-3px);
}

.ranking-title {
  padding: 18px;
  background-color: #fafbfc;
  font-weight: 700;
  font-size: 16px;
  border-bottom: 1px solid #f0f2f5;
  color: #303133;
  letter-spacing: 0.3px;
}

.stock-list {
  padding: 8px 0;
}

.stock-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  transition: background-color 0.2s ease;
  cursor: pointer;
}

.stock-item:hover {
  background-color: rgba(64, 158, 255, 0.05);
}

.stock-item:last-child {
  border-bottom: none;
}

.stock-info {
  display: flex;
  flex-direction: column;
}

.stock-name {
  font-weight: 600;
  margin-bottom: 4px;
  color: #303133;
  font-size: 15px;
}

.stock-code {
  font-size: 13px;
  color: #909399;
  font-weight: 500;
}

.stock-sector {
  font-size: 12px;
  color: #606266;
  background-color: #f0f2f5;
  padding: 2px 6px;
  border-radius: 4px;
  margin-top: 4px;
  display: inline-block;
  font-weight: 500;
}

.stock-change {
  font-weight: 700;
  font-size: 16px;
  border-radius: 4px;
  padding: 4px 8px;
}

.stock-up {
  color: #f56c6c; /* 红色表示上涨 */
  background-color: rgba(245, 108, 108, 0.1);
}

.stock-down {
  color: #67c23a; /* 绿色表示下跌 */
  background-color: rgba(103, 194, 58, 0.1);
}

.stock-volume {
  font-weight: 600;
  color: #606266;
  background-color: #f5f7fa;
  padding: 4px 8px;
  border-radius: 4px;
}

/* 消息区域样式 */
.messages-section {
  margin-top: 36px;
}

.message-list {
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
  padding: 8px 0;
  transition: all 0.3s ease;
}

.message-list:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.message-item {
  padding: 16px 18px;
  display: flex;
  align-items: center;
  gap: 14px;
  border-bottom: 1px solid #f5f7fa;
  transition: background-color 0.2s ease;
}

.message-item:hover {
  background-color: rgba(0, 0, 0, 0.01);
}

.message-item:last-child {
  border-bottom: none;
}

.message-item .el-icon {
  background-color: rgba(255, 107, 106, 0.1);
  padding: 8px;
  border-radius: 50%;
}

.message-text {
  color: #303133;
  font-size: 14px;
  font-weight: 500;
  line-height: 1.4;
}

/* 排行榜价格数据样式 */
.stock-price-data {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  text-align: right;
  min-width: 80px;
}

.current-price {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 2px;
}

/* 优化排行榜股票项目布局 */
.ranking-card .stock-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  transition: background-color 0.2s ease;
  cursor: pointer;
}

.ranking-card .stock-item:hover {
  background-color: rgba(64, 158, 255, 0.05);
}

.ranking-card .stock-item:last-child {
  border-bottom: none;
}

@media (max-width: 768px) {
  .dashboard {
    padding: 16px 12px;
  }

  .nav-tabs {
    margin-bottom: 16px;
  }

  .tab-item {
    padding: 10px 12px;
    margin-right: 16px;
    font-size: 14px;
  }

  .metrics-panel {
    flex-direction: column;
    gap: 16px;
    margin-bottom: 24px;
  }

  .metric-card {
    border-radius: 12px;
  }

  .card-header {
    padding: 14px 16px;
    font-size: 15px;
  }

  .metric-content {
    padding: 20px 16px;
  }

  .metric-value {
    font-size: 24px;
  }

  .section-header h2 {
    font-size: 18px;
  }

  .ranking-container {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .ranking-card {
    margin-bottom: 0;
  }

  .ranking-title {
    padding: 14px 16px;
    font-size: 15px;
  }

  .stock-item {
    padding: 12px 16px;
  }

  .stock-name {
    font-size: 14px;
  }

  .stock-code {
    font-size: 12px;
  }

  .stock-change {
    font-size: 14px;
    padding: 3px 6px;
  }

  .message-item {
    padding: 14px 16px;
    gap: 12px;
  }

  .message-text {
    font-size: 13px;
  }
  .el-col {
    width: 100% !important;
    flex: 0 0 100%;
    max-width: 100%;
  }

  .watchlist-section {
    margin-bottom: 24px;
  }

  .watchlist-item {
    padding: 12px 16px;
  }

  .current-price {
    font-size: 14px;
  }

  .stock-actions .el-button {
    font-size: 14px;
  }
}

@media (max-width: 480px) {
  .dashboard {
    padding: 12px 8px;
  }

  .nav-tabs {
    margin-bottom: 12px;
    padding-bottom: 4px;
  }

  .tab-item {
    padding: 8px 10px;
    margin-right: 12px;
    font-size: 13px;
    gap: 6px;
  }

  .metrics-panel {
    gap: 12px;
    margin-bottom: 20px;
  }

  .card-header {
    padding: 12px 14px;
    font-size: 14px;
  }

  .metric-content {
    padding: 16px 14px;
  }

  .metric-value {
    font-size: 20px;
  }

  .sub-text {
    font-size: 12px;
  }

  .section-header {
    margin-bottom: 16px;
  }

  .section-header h2 {
    font-size: 16px;
  }

  .ranking-container {
    gap: 12px;
  }

  .ranking-title {
    padding: 12px 14px;
    font-size: 14px;
  }

  .stock-item {
    padding: 10px 14px;
  }

  .stock-name {
    font-size: 13px;
    margin-bottom: 2px;
  }

  .stock-code {
    font-size: 11px;
  }

  .stock-change {
    font-size: 13px;
    padding: 2px 5px;
  }

  .stock-volume {
    font-size: 13px;
    padding: 2px 5px;
  }

  .messages-section {
    margin-top: 24px;
  }

  .message-item {
    padding: 12px 14px;
    gap: 10px;
  }

  .message-item .el-icon {
    padding: 6px;
  }

  .message-text {
    font-size: 12px;
    line-height: 1.3;
  }
}
</style>