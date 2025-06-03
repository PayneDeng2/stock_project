<template>
  <div class="trading-container">
    <!-- 用户活跃订单表格展示 -->
    <el-card class="pending-trade-card">
      <template #header>
        <div class="card-header">
          <span>我的活跃订单</span>
          <el-button size="small" @click="loadUserOrders">
            <el-icon><Refresh /></el-icon>刷新
          </el-button>
        </div>
      </template>
      <el-table :data="userActiveOrders" stripe style="width: 100%" v-if="userActiveOrders.length > 0">
        <el-table-column prop="stockName" label="股票名称" align="center" />
        <el-table-column prop="stockCode" label="股票代码" align="center" width="120" />
        <el-table-column prop="type" label="类型" align="center" width="80">
          <template #default="scope">
            <span :class="scope.row.type === 'BUY' ? 'profit' : 'loss'">
              {{ scope.row.type === 'BUY' ? '买入' : '卖出' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" align="center" width="100">
          <template #default="scope">
            ¥{{ scope.row.price.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="总量" align="center" width="80" />
        <el-table-column prop="remainingQuantity" label="剩余" align="center" width="80" />
        <el-table-column prop="status" label="状态" align="center" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" size="small">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="100">
          <template #default="scope">
            <el-button 
              size="small" 
              type="danger" 
              @click="cancelUserOrder(scope.row.id)"
              :disabled="scope.row.status === 'FILLED'"
            >
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-else class="empty-pending">
        <el-empty description="暂无活跃订单" />
      </div>
    </el-card>

    <!-- 交易操作表单 -->
    <el-card class="trading-card">
      <template #header>
        <div class="card-header">
          <span>股票交易操作</span>
        </div>
      </template>

      <el-form :model="tradeForm" label-width="100px" class="trading-form">
        <el-form-item label="股票代码">
          <el-select v-model="tradeForm.stockCode" placeholder="选择股票" style="width: 240px" @change="handleStockChange">
            <el-option
              v-for="stock in selectableStocks"
              :key="stock.code"
              :label="`${stock.name}(${stock.code})`"
              :value="stock.code"
            />
          </el-select>
        </el-form-item>

        <!-- 显示最优报价 -->
        <el-form-item label="当前报价" v-if="tradeForm.stockCode && bestQuote">
          <div class="quote-info">
            <span class="bid">买一: {{ bestQuote.bid ? `¥${bestQuote.bid.toFixed(2)}` : '--' }}</span>
            <span class="ask">卖一: {{ bestQuote.ask ? `¥${bestQuote.ask.toFixed(2)}` : '--' }}</span>
          </div>
        </el-form-item>

        <el-form-item label="交易类型">
          <el-radio-group v-model="tradeForm.type" @change="handleTypeChange">
            <el-radio-button label="BUY">买入</el-radio-button>
            <el-radio-button label="SELL">卖出</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="单价（¥）">
          <el-input-number v-model="tradeForm.price" :min="0.01" :step="0.01" style="width: 240px" />
        </el-form-item>

        <el-form-item label="数量">
          <el-input-number 
            v-model="tradeForm.quantity" 
            :min="minQuantity" 
            :max="maxQuantity" 
            :disabled="maxQuantity === 0"
            style="width: 240px" 
          />
          <div v-if="tradeForm.type === 'SELL' && tradeForm.stockCode" style="font-size: 12px; color: #909399; margin-top: 4px;">
            最大可卖: {{ maxSellQuantity }}股
          </div>
          <div v-if="maxQuantity === 0" style="font-size: 12px; color: #f56c6c; margin-top: 4px;">
            {{ tradeForm.type === 'SELL' ? '无可卖持仓' : '资金不足' }}
          </div>
        </el-form-item>

        <el-form-item label="总价（¥）">
          <div class="total-price">{{ totalPrice }}</div>
        </el-form-item>

        <el-form-item label="资金密码">
          <el-input
            v-model="tradeForm.password"
            type="password"
            show-password
            placeholder="请输入资金账户密码"
            style="width: 240px"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitOrder" :loading="isSubmitting">提交订单</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 最近成交记录 -->
    <el-card class="trades-card">
      <template #header>
        <div class="card-header">
          <span>最近成交记录</span>
          <el-button size="small" @click="loadRecentTrades">
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
        <el-table-column prop="timestamp" label="成交时间" align="center" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.timestamp) }}
          </template>
        </el-table-column>
      </el-table>
      <div v-else class="empty-pending">
        <el-empty description="暂无成交记录" />
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { getAllAvailableStocks } from '@/utils/stockDataService'
import { getPositions, checkSellAvailability } from '@/utils/positionManager'
import { checkFundsAvailability } from '@/utils/fundManager'
import { 
  submitOrder as submitOrderToMarket, 
  getUserActiveOrders, 
  cancelOrder, 
  getBestQuote, 
  getAllTrades
} from '@/utils/orderManager'

export default {
  name: 'TradingView',
  components: {
    Refresh
  },
  setup() {
    const tradeForm = ref({
      stockCode: '',
      type: 'BUY',
      price: 0,
      quantity: 0,
      password: ''
    })

    const isSubmitting = ref(false)
    const userActiveOrders = ref([])
    const recentTrades = ref([])
    const bestQuote = ref(null)

    // 使用完整的股票列表
    const availableStocks = ref([])
    
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

    // 根据交易类型计算可选择的股票列表
    const selectableStocks = computed(() => {
      if (tradeForm.value.type === 'SELL') {
        // 卖出时只显示有持仓的股票
        const positions = getPositions()
        return positions.map(pos => ({
          name: pos.stockName,
          code: pos.stockCode,
          quantity: pos.quantity
        }))
      } else {
        // 买入时显示所有可交易股票
        return availableStocks.value
      }
    })

    // 计算当前选中股票的最大可卖数量
    const maxSellQuantity = computed(() => {
      if (tradeForm.value.type === 'SELL' && tradeForm.value.stockCode) {
        const positions = getPositions()
        const position = positions.find(pos => pos.stockCode === tradeForm.value.stockCode)
        return position ? position.quantity : 0
      }
      return 0
    })

    // 计算最大可操作数量
    const maxQuantity = computed(() => {
      if (tradeForm.value.type === 'SELL') {
        return maxSellQuantity.value
      }
      // 买入时根据资金计算最大可买数量
      if (tradeForm.value.price > 0) {
        const fundsCheck = checkFundsAvailability(0)
        return Math.floor(fundsCheck.currentFunds / tradeForm.value.price)
      }
      return Number.MAX_SAFE_INTEGER
    })

    // 计算最小可操作数量，确保不会大于最大数量
    const minQuantity = computed(() => {
      const max = maxQuantity.value
      if (max === 0) {
        return 0
      }
      return Math.min(1, max)
    })

    // 监听交易类型变化
    const handleTypeChange = () => {
      tradeForm.value.stockCode = ''
      tradeForm.value.quantity = 0
      bestQuote.value = null
    }

    // 监听股票变化
    const handleStockChange = () => {
      if (tradeForm.value.stockCode) {
        // 重置数量
        tradeForm.value.quantity = 0
        
        // 获取最优报价
        bestQuote.value = getBestQuote(tradeForm.value.stockCode)
        
        // 根据最优报价设置建议价格
        if (tradeForm.value.type === 'BUY' && bestQuote.value.ask) {
          tradeForm.value.price = bestQuote.value.ask
        } else if (tradeForm.value.type === 'SELL' && bestQuote.value.bid) {
          tradeForm.value.price = bestQuote.value.bid
        }
      }
    }

    // 获取股票名称
    const getStockName = (stockCode) => {
      const stock = availableStocks.value.find(s => s.code === stockCode)
      return stock ? stock.name : '未知股票'
    }

    const totalPrice = computed(() => {
      const p = Number(tradeForm.value.price)
      const q = Number(tradeForm.value.quantity)
      return isNaN(p * q) ? '0.00' : (p * q).toFixed(2)
    })

    // 提交订单
    const submitOrder = async () => {
      const { stockCode, price, quantity, password } = tradeForm.value
      
      // 基础验证
      if (!stockCode || !price || !quantity || !password) {
        ElMessage.warning('请填写完整订单信息')
        return
      }
      
      if (password !== '123456') {
        ElMessage.error('资金账户密码错误')
        return
      }
      
      const tradeAmount = parseFloat(totalPrice.value)
      
      // 买入操作需要验证资金
      if (tradeForm.value.type === 'BUY') {
        const fundsCheck = checkFundsAvailability(tradeAmount)
        if (!fundsCheck.available) {
          ElMessage.error(`可用资金不足，当前可用资金: ¥${fundsCheck.currentFunds.toFixed(2)}，需要资金: ¥${fundsCheck.requiredFunds.toFixed(2)}`)
          return
        }
      }
      
      // 卖出操作需要验证持仓
      if (tradeForm.value.type === 'SELL') {
        const sellCheck = checkSellAvailability(stockCode, quantity)
        if (!sellCheck.available) {
          ElMessage.error(sellCheck.message)
          return
        }
      }
      
      isSubmitting.value = true
      
      try {
        // 提交订单到撮合系统
        const orderData = {
          stockCode: stockCode,
          stockName: getStockName(stockCode),
          type: tradeForm.value.type,
          price: price,
          quantity: quantity
        }
        
        const result = submitOrderToMarket(orderData)
        
        if (result.success) {
          ElMessage.success(result.message)
          
          // 重新加载数据
          loadUserOrders()
          loadRecentTrades()
          
          // 更新最优报价
          if (tradeForm.value.stockCode) {
            bestQuote.value = getBestQuote(tradeForm.value.stockCode)
          }
          
          // 重置表单
          resetForm()
        } else {
          ElMessage.error(result.error || '订单提交失败')
        }
      } catch (error) {
        console.error('提交订单失败:', error)
        ElMessage.error('订单提交失败: ' + error.message)
      } finally {
        isSubmitting.value = false
      }
    }

    // 加载用户活跃订单
    const loadUserOrders = () => {
      userActiveOrders.value = getUserActiveOrders()
    }

    // 加载最近成交记录
    const loadRecentTrades = () => {
      const allTrades = getAllTrades()
      recentTrades.value = allTrades.slice(0, 20) // 显示最近20条
    }

    // 取消订单
    const cancelUserOrder = async (orderId) => {
      try {
        const result = cancelOrder(orderId)
        if (result.success) {
          ElMessage.success(result.message)
          loadUserOrders() // 重新加载订单列表
          
          // 更新最优报价
          if (tradeForm.value.stockCode) {
            bestQuote.value = getBestQuote(tradeForm.value.stockCode)
          }
        } else {
          ElMessage.error(result.message)
        }
      } catch (error) {
        console.error('取消订单失败:', error)
        ElMessage.error('取消订单失败')
      }
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

    const resetForm = () => {
      tradeForm.value = {
        stockCode: '',
        type: 'BUY',
        price: 0,
        quantity: 0,
        password: ''
      }
      bestQuote.value = null
    }

    // 监听用户切换
    const currentUser = ref(sessionStorage.getItem('loggedInUserDemo'))
    
    watch(() => sessionStorage.getItem('loggedInUserDemo'), (newUser) => {
      if (newUser !== currentUser.value) {
        console.log('用户切换:', currentUser.value, '->', newUser)
        currentUser.value = newUser
        
        // 重新加载数据
        loadUserOrders()
        loadRecentTrades()
        
        // 清空表单
        resetForm()
        
        ElMessage.info(`已切换至用户: ${newUser || '默认用户'}`)
      }
    })

    // 监听数量变化，确保在有效范围内
    watch(() => tradeForm.value.quantity, (newQuantity) => {
      if (newQuantity > maxQuantity.value) {
        tradeForm.value.quantity = maxQuantity.value
      } else if (newQuantity < 0) {
        tradeForm.value.quantity = 0
      }
    })

    // 监听maxQuantity变化，调整当前数量
    watch(() => maxQuantity.value, (newMax) => {
      if (tradeForm.value.quantity > newMax) {
        tradeForm.value.quantity = newMax > 0 ? Math.min(tradeForm.value.quantity, newMax) : 0
      }
    })

    // 组件挂载时初始化数据
    onMounted(() => {
      // 初始化股票列表
      initializeStocks()
      
      // 加载用户数据
      loadUserOrders()
      loadRecentTrades()
      
      // 检查是否有预选的股票信息
      const selectedStock = sessionStorage.getItem('selectedStockForTrade')
      if (selectedStock) {
        try {
          const stockData = JSON.parse(selectedStock)
          tradeForm.value.stockCode = stockData.code
          if (stockData.currentPrice) {
            tradeForm.value.price = parseFloat(stockData.currentPrice)
          }
          
          // 获取最优报价
          bestQuote.value = getBestQuote(stockData.code)
          
          sessionStorage.removeItem('selectedStockForTrade')
          ElMessage.success(`已自动选择股票: ${stockData.name}`)
        } catch (error) {
          console.error('解析预选股票信息失败:', error)
        }
      }
    })

    return {
      tradeForm,
      isSubmitting,
      userActiveOrders,
      recentTrades,
      bestQuote,
      availableStocks,
      selectableStocks,
      maxSellQuantity,
      maxQuantity,
      minQuantity,
      totalPrice,
      submitOrder,
      loadUserOrders,
      loadRecentTrades,
      cancelUserOrder,
      getStatusType,
      getStatusText,
      formatTime,
      resetForm,
      handleTypeChange,
      handleStockChange
    }
  }
}
</script>

<style scoped>
.trading-container {
  padding: 30px;
}

.trading-card, .trades-card {
  max-width: 800px;
  margin: 20px auto;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.pending-trade-card {
  margin-bottom: 40px;
  max-width: 1200px;
  margin: 0 auto 40px auto;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  font-size: 20px;
  font-weight: bold;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.trading-form {
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.total-price {
  font-size: 18px;
  font-weight: bold;
  color: #409eff;
  line-height: 32px;
}

.quote-info {
  display: flex;
  gap: 20px;
  align-items: center;
}

.quote-info .bid {
  color: #67c23a;
  font-weight: 500;
}

.quote-info .ask {
  color: #f56c6c;
  font-weight: 500;
}

.el-table .cell {
  font-size: 14px;
}

.profit {
  color: #67c23a;
}

.loss {
  color: #f56c6c;
}

.el-form-item {
  width: 100%;
  max-width: 400px;
  justify-content: center;
}

.empty-pending {
  padding: 40px 0;
}

/* 移动端响应式样式 */
@media screen and (max-width: 768px) {
  .trading-container {
    padding: 15px 10px;
  }
  
  .pending-trade-card,
  .trading-card,
  .trades-card {
    margin: 10px 0;
    max-width: 100%;
  }
  
  .trading-form {
    padding: 15px;
  }
  
  .el-form-item {
    max-width: 100%;
  }
  
  .card-header {
    font-size: 18px;
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }
  
  .quote-info {
    flex-direction: column;
    gap: 8px;
    align-items: flex-start;
  }
  
  /* 表格在移动端的优化 */
  :deep(.el-table) {
    font-size: 12px;
  }
  
  :deep(.el-table th),
  :deep(.el-table td) {
    padding: 8px 4px;
  }
  
  :deep(.el-table__header-wrapper) {
    overflow-x: auto;
  }
}

@media screen and (max-width: 480px) {
  .trading-container {
    padding: 10px 5px;
  }
  
  .trading-form {
    padding: 10px;
  }
  
  .card-header {
    font-size: 16px;
  }
  
  .total-price {
    font-size: 16px;
  }
  
  /* 手机端表格进一步压缩 */
  :deep(.el-table) {
    font-size: 11px;
  }
  
  :deep(.el-table th),
  :deep(.el-table td) {
    padding: 6px 2px;
  }
  
  /* 隐藏部分列在极小屏幕上 */
  :deep(.el-table__column--hidden) {
    display: none;
  }
}
</style>
