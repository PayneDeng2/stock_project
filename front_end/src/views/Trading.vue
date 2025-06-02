<template>
  <div class="trading-container">
    <!-- 待匹配交易表格展示 -->
    <el-card class="pending-trade-card">
      <template #header>
        <div class="card-header">
          <span>待匹配交易</span>
        </div>
      </template>      <el-table :data="pendingTrades" stripe style="width: 100%">
        <el-table-column prop="date" label="交易日期" align="center" width="150" />
        <el-table-column prop="stockName" label="股票名称" align="center" />
        <el-table-column prop="stockCode" label="股票代码" align="center" width="120" />
        <el-table-column prop="type" label="交易类型" align="center" width="100">
          <template #default="scope">
            <span :class="scope.row.type === '买入' ? 'profit' : 'loss'">
              {{ scope.row.type }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="交易价格" align="center" />
        <el-table-column prop="quantity" label="交易数量" align="center" />
        <el-table-column prop="amount" label="交易金额" align="center" />
      </el-table>
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
          <el-select v-model="tradeForm.stockCode" placeholder="选择股票" style="width: 240px">
            <el-option
              v-for="stock in availableStocks"
              :key="stock.code"
              :label="`${stock.name}(${stock.code})`"
              :value="stock.code"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="交易类型">
          <el-radio-group v-model="tradeForm.type">
            <el-radio-button label="买入" />
            <el-radio-button label="卖出" />
          </el-radio-group>
        </el-form-item>

        <el-form-item label="单价（¥）">
          <el-input-number v-model="tradeForm.price" :min="0.01" :step="0.01" style="width: 240px" />
        </el-form-item>

        <el-form-item label="数量">
          <el-input-number v-model="tradeForm.quantity" :min="1" style="width: 240px" />
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
          <el-button type="primary" @click="submitTrade">提交交易</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { addTradeRecord, addPendingTrade, getPendingTrades, getStockName } from '@/utils/tradeManager'

export default {
  name: 'TradingView',
  setup() {
    const tradeForm = ref({
      stockCode: '',
      type: '买入',
      price: 0,
      quantity: 0,
      password: ''
    })

    const availableStocks = ref([
      { name: '阿里巴巴', code: '9988.HK' },
      { name: '腾讯控股', code: '0700.HK' },
      { name: '贵州茅台', code: '600519.SH' },
      { name: '中国平安', code: '601318.SH' }
    ])

    const pendingTrades = ref([]) // 待匹配交易记录    // 组件挂载时加载待匹配交易记录
    onMounted(() => {
      const rawPendingTrades = getPendingTrades()
      // 将待匹配交易格式化为与Account.vue一致的格式
      pendingTrades.value = rawPendingTrades.map(trade => ({
        ...trade,
        date: trade.time ? new Date(trade.time).toLocaleDateString('zh-CN') : 
              (trade.date || new Date().toLocaleDateString('zh-CN')),
        stockName: getStockName(trade.stockCode),
        amount: trade.totalPrice || trade.amount || '0.00'
      }))
      
      // 检查是否有预选的股票信息
      const selectedStock = sessionStorage.getItem('selectedStockForTrade')
      if (selectedStock) {
        try {
          const stockData = JSON.parse(selectedStock)
          // 预填充股票信息
          tradeForm.value.stockCode = stockData.code
          if (stockData.currentPrice) {
            tradeForm.value.price = parseFloat(stockData.currentPrice)
          }
          // 清除sessionStorage中的数据
          sessionStorage.removeItem('selectedStockForTrade')
          
          ElMessage.success(`已自动选择股票: ${stockData.name}`)
        } catch (error) {
          console.error('解析预选股票信息失败:', error)
        }
      }
    })

    const totalPrice = computed(() => {
      const p = Number(tradeForm.value.price)
      const q = Number(tradeForm.value.quantity)
      return isNaN(p * q) ? '0.00' : (p * q).toFixed(2)
    })

    const submitTrade = () => {
      const { stockCode, price, quantity, password } = tradeForm.value
      if (!stockCode || !price || !quantity || !password) {
        ElMessage.warning('请填写完整交易信息')
        return
      }
      if ((price * 100) % 1 > 0) {
        ElMessage.warning('请输入有效的单价')
        return
      }
      if (quantity % 1 > 0) {
        ElMessage.warning('请输入有效的数量')
        return
      }
      if (password !== '123456') {
        ElMessage.error('资金账户密码错误')
        return
      }
        // 创建交易记录对象
      const tradeData = {
        stockCode,
        type: tradeForm.value.type,
        price: price.toFixed(2),
        quantity,
        totalPrice: totalPrice.value,
        time: new Date().toLocaleString()
      }
      
      // 添加到待匹配交易记录（本地显示），格式化为与Account.vue一致的格式
      const formattedPendingTrade = {
        ...tradeData,
        date: new Date().toLocaleDateString('zh-CN'),
        stockName: getStockName(stockCode),
        amount: totalPrice.value,
        id: Date.now() + Math.random(),
        timestamp: new Date().getTime()
      }
      
      const pendingTrade = addPendingTrade(tradeData)
      if (pendingTrade) {
        pendingTrades.value.unshift(formattedPendingTrade)
      }
      
      // 同时添加到交易记录（模拟立即执行）
      const completedTrade = addTradeRecord(tradeData)
      
      if (completedTrade) {
        ElMessage.success('成功提交订单，交易已记录到账户中')
      } else {
        ElMessage.error('订单提交失败')
        return
      }

      // 清空表单数据
      resetForm()
    }

    const resetForm = () => {
      tradeForm.value = {
        stockCode: '',
        type: '买入',
        price: 0,
        quantity: 0,
        password: ''
      }
    }

    return {
      tradeForm,
      availableStocks,
      pendingTrades,
      totalPrice,
      submitTrade,
      resetForm
    }
  }
}
</script>

<style scoped>
.trading-container {
  padding: 30px;
}

.trading-card {
  max-width: 800px;
  margin: 20px auto;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.pending-trade-card {
  margin-bottom: 40px;
  max-width: 1200px;
  margin: 0 auto;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  font-size: 20px;
  font-weight: bold;
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

/* 移动端响应式样式 */
@media screen and (max-width: 768px) {
  .trading-container {
    padding: 15px 10px;
  }
  
  .pending-trade-card,
  .trading-card {
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
