<template>
  <div class="orderbook-container">
    <el-card class="orderbook-card">
      <template #header>
        <div class="card-header">
          <span>全局订单簿</span>
          <div class="header-actions">
            <el-select v-model="selectedStock" placeholder="选择股票" style="width: 200px" @change="handleStockSelect">
              <el-option
                v-for="stock in availableStocks"
                :key="stock.code"
                :label="`${stock.name}(${stock.code})`"
                :value="stock.code"
              />
            </el-select>
            <el-button @click="loadOrderBook">
              <el-icon><Refresh /></el-icon>刷新
            </el-button>
          </div>
        </div>
      </template>

      <div v-if="selectedStock && stockOrderBook" class="orderbook-content">
        <div class="stock-info">
          <h3>{{ getStockName(selectedStock) }} ({{ selectedStock }})</h3>
          <div class="best-quote" v-if="bestQuote.bid || bestQuote.ask">
            <span class="bid">买一: {{ bestQuote.bid ? `¥${bestQuote.bid.toFixed(2)}` : '--' }}</span>
            <span class="spread">价差: {{ spread ? `¥${spread.toFixed(2)}` : '--' }}</span>
            <span class="ask">卖一: {{ bestQuote.ask ? `¥${bestQuote.ask.toFixed(2)}` : '--' }}</span>
          </div>
        </div>

        <div class="orderbook-tables">
          <!-- 买单表格 -->
          <div class="buy-orders">
            <h4>买单 ({{ stockOrderBook.buyOrders.length }})</h4>
            <el-table 
              :data="sortedBuyOrders" 
              stripe 
              style="width: 100%" 
              :max-height="400"
              empty-text="暂无买单"
            >
              <el-table-column prop="price" label="价格" align="center" width="100">
                <template #default="scope">
                  <span class="price buy-price">¥{{ scope.row.price.toFixed(2) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="remainingQuantity" label="数量" align="center" width="80" />
              <el-table-column prop="userId" label="用户" align="center" width="100" />
              <el-table-column prop="timestamp" label="时间" align="center" width="140">
                <template #default="scope">
                  {{ formatTime(scope.row.timestamp) }}
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" align="center" width="80">
                <template #default="scope">
                  <el-tag :type="getStatusType(scope.row.status)" size="small">
                    {{ getStatusText(scope.row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- 卖单表格 -->
          <div class="sell-orders">
            <h4>卖单 ({{ stockOrderBook.sellOrders.length }})</h4>
            <el-table 
              :data="sortedSellOrders" 
              stripe 
              style="width: 100%" 
              :max-height="400"
              empty-text="暂无卖单"
            >
              <el-table-column prop="price" label="价格" align="center" width="100">
                <template #default="scope">
                  <span class="price sell-price">¥{{ scope.row.price.toFixed(2) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="remainingQuantity" label="数量" align="center" width="80" />
              <el-table-column prop="userId" label="用户" align="center" width="100" />
              <el-table-column prop="timestamp" label="时间" align="center" width="140">
                <template #default="scope">
                  {{ formatTime(scope.row.timestamp) }}
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" align="center" width="80">
                <template #default="scope">
                  <el-tag :type="getStatusType(scope.row.status)" size="small">
                    {{ getStatusText(scope.row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </div>

      <div v-else class="empty-state">
        <el-empty description="请选择股票查看订单簿" />
      </div>
    </el-card>

    <!-- 全局成交记录 -->
    <el-card class="trades-card">
      <template #header>
        <div class="card-header">
          <span>全局成交记录</span>
          <el-button @click="loadTrades">
            <el-icon><Refresh /></el-icon>刷新
          </el-button>
        </div>
      </template>
      <el-table :data="recentTrades" stripe style="width: 100%" v-if="recentTrades.length > 0">
        <el-table-column prop="stockName" label="股票名称" align="center" />
        <el-table-column prop="stockCode" label="股票代码" align="center" width="120" />
        <el-table-column prop="price" label="成交价" align="center" width="100">
          <template #default="scope">
            ¥{{ scope.row.price.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="成交量" align="center" width="100" />
        <el-table-column prop="amount" label="成交额" align="center" width="120">
          <template #default="scope">
            ¥{{ scope.row.amount.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="buyUserId" label="买方" align="center" width="100" />
        <el-table-column prop="sellUserId" label="卖方" align="center" width="100" />
        <el-table-column prop="timestamp" label="成交时间" align="center" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.timestamp) }}
          </template>
        </el-table-column>
      </el-table>
      <div v-else class="empty-state">
        <el-empty description="暂无成交记录" />
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { getAllAvailableStocks } from '@/utils/stockDataService'
import { getOrderBook, getBestQuote, getAllTrades } from '@/utils/orderManager'

export default {
  name: 'OrderBookView',
  components: {
    Refresh
  },
  setup() {
    const selectedStock = ref('')
    const availableStocks = ref([])
    const stockOrderBook = ref(null)
    const recentTrades = ref([])
    
    // 初始化股票列表
    const initializeStocks = () => {
      const allStocks = getAllAvailableStocks()
      availableStocks.value = allStocks.map(stock => ({
        name: stock.name,
        code: stock.code,
        sector: stock.sector,
        market: stock.market
      }))
    }

    // 获取股票名称
    const getStockName = (stockCode) => {
      const stock = availableStocks.value.find(s => s.code === stockCode)
      return stock ? stock.name : '未知股票'
    }

    // 最优报价
    const bestQuote = computed(() => {
      if (!selectedStock.value) return { bid: null, ask: null }
      return getBestQuote(selectedStock.value)
    })

    // 价差
    const spread = computed(() => {
      if (bestQuote.value.bid && bestQuote.value.ask) {
        return bestQuote.value.ask - bestQuote.value.bid
      }
      return null
    })

    // 排序后的买单（价格降序）
    const sortedBuyOrders = computed(() => {
      if (!stockOrderBook.value) return []
      return [...stockOrderBook.value.buyOrders].sort((a, b) => {
        if (a.price !== b.price) return b.price - a.price
        return a.timestamp - b.timestamp
      })
    })

    // 排序后的卖单（价格升序）
    const sortedSellOrders = computed(() => {
      if (!stockOrderBook.value) return []
      return [...stockOrderBook.value.sellOrders].sort((a, b) => {
        if (a.price !== b.price) return a.price - b.price
        return a.timestamp - b.timestamp
      })
    })

    // 处理股票选择
    const handleStockSelect = () => {
      if (selectedStock.value) {
        loadStockOrderBook()
      } else {
        stockOrderBook.value = null
      }
    }

    // 加载选中股票的订单簿
    const loadStockOrderBook = () => {
      if (!selectedStock.value) return
      
      const orderBook = getOrderBook()
      stockOrderBook.value = orderBook[selectedStock.value] || {
        buyOrders: [],
        sellOrders: []
      }
    }

    // 加载整个订单簿
    const loadOrderBook = () => {
      if (selectedStock.value) {
        loadStockOrderBook()
      }
    }

    // 加载成交记录
    const loadTrades = () => {
      const allTrades = getAllTrades()
      recentTrades.value = allTrades.slice(0, 50) // 显示最近50条
    }

    // 获取订单状态类型
    const getStatusType = (status) => {
      const statusMap = {
        'PENDING': '',
        'PARTIAL': 'warning', 
        'FILLED': 'success',
        'CANCELLED': 'danger'
      }
      return statusMap[status] || ''
    }

    // 获取订单状态文本
    const getStatusText = (status) => {
      const statusMap = {
        'PENDING': '等待',
        'PARTIAL': '部分',
        'FILLED': '完成',
        'CANCELLED': '取消'
      }
      return statusMap[status] || status
    }

    // 格式化时间
    const formatTime = (timestamp) => {
      return new Date(timestamp).toLocaleString('zh-CN')
    }

    // 监听用户切换
    watch(() => sessionStorage.getItem('loggedInUserDemo'), () => {
      // 用户切换时重新加载数据
      loadOrderBook()
      loadTrades()
    })

    // 组件挂载时初始化
    onMounted(() => {
      initializeStocks()
      loadTrades()
    })

    return {
      selectedStock,
      availableStocks,
      stockOrderBook,
      recentTrades,
      bestQuote,
      spread,
      sortedBuyOrders,
      sortedSellOrders,
      getStockName,
      handleStockSelect,
      loadOrderBook,
      loadTrades,
      getStatusType,
      getStatusText,
      formatTime
    }
  }
}
</script>

<style scoped>
.orderbook-container {
  padding: 30px;
}

.orderbook-card, .trades-card {
  margin-bottom: 30px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 20px;
  font-weight: bold;
}

.header-actions {
  display: flex;
  gap: 15px;
  align-items: center;
}

.stock-info {
  margin-bottom: 30px;
  text-align: center;
}

.stock-info h3 {
  margin: 0 0 15px 0;
  color: #303133;
}

.best-quote {
  display: flex;
  justify-content: center;
  gap: 30px;
  align-items: center;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.best-quote .bid {
  color: #67c23a;
  font-weight: 600;
  font-size: 16px;
}

.best-quote .ask {
  color: #f56c6c;
  font-weight: 600;
  font-size: 16px;
}

.best-quote .spread {
  color: #909399;
  font-size: 14px;
}

.orderbook-tables {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
}

.buy-orders h4, .sell-orders h4 {
  margin: 0 0 15px 0;
  text-align: center;
  padding: 10px;
  border-radius: 6px;
}

.buy-orders h4 {
  background-color: #f0f9ff;
  color: #67c23a;
}

.sell-orders h4 {
  background-color: #fef2f2;
  color: #f56c6c;
}

.price.buy-price {
  color: #67c23a;
  font-weight: 600;
}

.price.sell-price {
  color: #f56c6c;
  font-weight: 600;
}

.empty-state {
  padding: 60px 0;
}

/* 移动端响应式 */
@media screen and (max-width: 768px) {
  .orderbook-container {
    padding: 15px 10px;
  }
  
  .card-header {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
    font-size: 18px;
  }
  
  .header-actions {
    width: 100%;
    flex-direction: column;
    gap: 10px;
  }
  
  .header-actions .el-select {
    width: 100% !important;
  }
  
  .best-quote {
    flex-direction: column;
    gap: 10px;
  }
  
  .orderbook-tables {
    grid-template-columns: 1fr;
    gap: 20px;
  }
  
  :deep(.el-table) {
    font-size: 12px;
  }
  
  :deep(.el-table th),
  :deep(.el-table td) {
    padding: 6px 4px;
  }
}
</style> 