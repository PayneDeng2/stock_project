<template>
  <div class="trading-container">
    <!-- 待匹配交易表格展示 -->
    <el-card class="pending-trade-card">
      <template #header>
        <div class="card-header">
          <span>待匹配交易</span>
        </div>
      </template>
      <el-table :data="pendingTrades" stripe style="width: 100%" v-if="pendingTrades.length > 0">
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
      <div v-else class="empty-pending">
        <el-empty description="暂无待匹配交易" />
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

    const pendingTrades = ref([]) // 待匹配交易记录

    // 获取股票名称
    const getStockName = (stockCode) => {
      const stock = availableStocks.value.find(s => s.code === stockCode)
      return stock ? stock.name : '未知股票'
    }

    // 从localStorage获取交易记录
    const getTradeRecords = () => {
      const records = localStorage.getItem('tradeRecords')
      return records ? JSON.parse(records) : []
    }

    // 保存交易记录到localStorage
    const saveTradeRecord = (tradeData) => {
      const records = getTradeRecords()
      const newRecord = {
        id: Date.now(),
        date: new Date().toLocaleDateString(),
        stockName: getStockName(tradeData.stockCode),
        stockCode: tradeData.stockCode,
        type: tradeData.type,
        price: `¥${tradeData.price}`,
        quantity: tradeData.quantity,
        amount: `¥${tradeData.totalPrice}`,
        timestamp: Date.now()
      }
      records.unshift(newRecord)
      localStorage.setItem('tradeRecords', JSON.stringify(records))
      return newRecord
    }

    // 获取持仓数据
    const getPositions = () => {
      const positions = localStorage.getItem('positions')
      return positions ? JSON.parse(positions) : [
        {
          stockName: '阿里巴巴',
          stockCode: '9988.HK',
          quantity: 100,
          avgPrice: 88.50,
          currentPrice: 89.75,
          profitLoss: 1.41
        },
        {
          stockName: '腾讯控股',
          stockCode: '0700.HK',
          quantity: 30,
          avgPrice: 365.00,
          currentPrice: 368.20,
          profitLoss: 0.88
        },
        {
          stockName: '小米集团',
          stockCode: '1810.HK',
          quantity: 500,
          avgPrice: 12.80,
          currentPrice: 12.46,
          profitLoss: -2.66
        }
      ]
    }

    // 更新持仓数据
    const updatePositions = (stockCode, type, quantity, price) => {
      const positions = getPositions()
      const stockName = getStockName(stockCode)
      
      // 查找现有持仓
      let existingPosition = positions.find(pos => pos.stockCode === stockCode)
      
      if (type === '买入') {
        if (existingPosition) {
          // 更新现有持仓
          const totalQuantity = existingPosition.quantity + quantity
          const totalCost = (existingPosition.quantity * existingPosition.avgPrice) + (quantity * parseFloat(price))
          existingPosition.quantity = totalQuantity
          existingPosition.avgPrice = parseFloat((totalCost / totalQuantity).toFixed(2))
          // 重新计算盈亏
          existingPosition.profitLoss = parseFloat((((existingPosition.currentPrice - existingPosition.avgPrice) / existingPosition.avgPrice) * 100).toFixed(2))
        } else {
          // 新增持仓
          positions.push({
            stockName: stockName,
            stockCode: stockCode,
            quantity: quantity,
            avgPrice: parseFloat(price),
            currentPrice: parseFloat(price),
            profitLoss: 0
          })
        }
      } else if (type === '卖出') {
        if (existingPosition && existingPosition.quantity >= quantity) {
          existingPosition.quantity -= quantity
          // 如果卖完了，从持仓中移除
          if (existingPosition.quantity === 0) {
            const index = positions.findIndex(pos => pos.stockCode === stockCode)
            positions.splice(index, 1)
          }
        }
      }
      
      localStorage.setItem('positions', JSON.stringify(positions))
    }

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
      
      // 添加到待匹配交易记录，格式化为与Account.vue一致的格式
      const formattedPendingTrade = {
        ...tradeData,
        date: new Date().toLocaleDateString('zh-CN'),
        stockName: getStockName(stockCode),
        amount: `¥${totalPrice.value}`,
        price: `¥${price.toFixed(2)}`,
        id: Date.now() + Math.random(),
        timestamp: new Date().getTime()
      }
      
      // 添加到待匹配交易列表
      pendingTrades.value.unshift(formattedPendingTrade)
      
      ElMessage.success('交易订单已提交，等待撮合...')
      
      // 2秒后自动完成交易
      setTimeout(() => {
        // 从待匹配列表中移除这个交易
        const tradeIndex = pendingTrades.value.findIndex(trade => trade.id === formattedPendingTrade.id)
        if (tradeIndex !== -1) {
          pendingTrades.value.splice(tradeIndex, 1)
        }
        
        // 保存到交易记录
        saveTradeRecord(tradeData)
        
        // 更新持仓
        updatePositions(stockCode, tradeForm.value.type, quantity, price.toFixed(2))
        
        ElMessage.success('交易撮合成功！')
      }, 2000)

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

    // 组件挂载时初始化数据
    onMounted(() => {
      // 初始化默认交易记录（如果没有的话）
      if (getTradeRecords().length === 0) {
        const defaultRecords = [
          {
            id: 1,
            date: '2025/6/1',
            stockName: '阿里巴巴',
            stockCode: '9988.HK',
            type: '买入',
            price: '¥88.50',
            quantity: 100,
            amount: '¥8,850.00',
            timestamp: Date.now() - 86400000
          },
          {
            id: 2,
            date: '2025/5/31',
            stockName: '腾讯控股',
            stockCode: '0700.HK',
            type: '买入',
            price: '¥365.00',
            quantity: 30,
            amount: '¥10,950.00',
            timestamp: Date.now() - 172800000
          }
        ]
        localStorage.setItem('tradeRecords', JSON.stringify(defaultRecords))
      }
      
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
  margin: 0 auto 40px auto;
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

.empty-pending {
  padding: 40px 0;
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