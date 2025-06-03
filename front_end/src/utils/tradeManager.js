/**
 * 交易记录管理工具
 * 用于在前端管理交易记录的本地存储和同步
 * 支持多用户数据隔离
 */

const BASE_TRADE_RECORDS_KEY = 'stock_trade_records'
const BASE_PENDING_TRADES_KEY = 'stock_pending_trades'

/**
 * 获取当前登录用户名
 */
function getCurrentUsername() {
  return sessionStorage.getItem('loggedInUserDemo') || 'default'
}

/**
 * 获取用户专属的交易记录存储key
 */
function getUserTradeRecordsKey() {
  const username = getCurrentUsername()
  return `${BASE_TRADE_RECORDS_KEY}_${username}`
}

/**
 * 获取用户专属的待匹配交易存储key
 */
function getUserPendingTradesKey() {
  const username = getCurrentUsername()
  return `${BASE_PENDING_TRADES_KEY}_${username}`
}

// 股票代码到名称的映射
const stockCodeToName = {
  '9988.HK': '阿里巴巴',
  '0700.HK': '腾讯控股', 
  '600519.SH': '贵州茅台',
  '601318.SH': '中国平安',
  '600036.SH': '招商银行',
  '9618.HK': '京东集团',
  '3690.HK': '美团点评',
  '1810.HK': '小米集团',
  '601857.SH': '中国石油',
  '601398.SH': '中国工商银行'
}

/**
 * 获取股票名称
 */
export function getStockName(stockCode) {
  return stockCodeToName[stockCode] || stockCode
}

/**
 * 获取所有交易记录
 */
export function getTradeRecords() {
  try {
    const userKey = getUserTradeRecordsKey()
    const records = localStorage.getItem(userKey)
    console.log(`获取用户 ${getCurrentUsername()} 的交易记录，key: ${userKey}`)
    return records ? JSON.parse(records) : []
  } catch (error) {
    console.error('获取交易记录失败:', error)
    return []
  }
}

/**
 * 添加新的交易记录
 */
export function addTradeRecord(record) {
  try {
    const records = getTradeRecords()
    const newRecord = {
      id: Date.now() + Math.random(), // 生成唯一ID
      date: new Date().toLocaleDateString('zh-CN'),
      stockName: getStockName(record.stockCode),
      stockCode: record.stockCode,
      type: record.type,
      price: parseFloat(record.price),
      quantity: parseInt(record.quantity),
      amount: parseFloat(record.totalPrice),
      timestamp: new Date().getTime()
    }
    
    // 新记录插入到数组开头
    records.unshift(newRecord)
    
    // 限制记录数量，保留最近100条
    if (records.length > 100) {
      records.splice(100)
    }
    
    const userKey = getUserTradeRecordsKey()
    localStorage.setItem(userKey, JSON.stringify(records))
    console.log(`保存用户 ${getCurrentUsername()} 的交易记录，key: ${userKey}`)
    return newRecord
  } catch (error) {
    console.error('添加交易记录失败:', error)
    return null
  }
}

/**
 * 获取待匹配交易记录
 */
export function getPendingTrades() {
  try {
    const userKey = getUserPendingTradesKey()
    const trades = localStorage.getItem(userKey)
    console.log(`获取用户 ${getCurrentUsername()} 的待匹配交易，key: ${userKey}`)
    return trades ? JSON.parse(trades) : []
  } catch (error) {
    console.error('获取待匹配交易失败:', error)
    return []
  }
}

/**
 * 添加待匹配交易记录
 */
export function addPendingTrade(trade) {
  try {
    const trades = getPendingTrades()
    const newTrade = {
      ...trade,
      id: Date.now() + Math.random(),
      timestamp: new Date().getTime()
    }
    
    trades.unshift(newTrade)
    
    // 限制待匹配交易数量
    if (trades.length > 50) {
      trades.splice(50)
    }
    
    const userKey = getUserPendingTradesKey()
    localStorage.setItem(userKey, JSON.stringify(trades))
    console.log(`保存用户 ${getCurrentUsername()} 的待匹配交易，key: ${userKey}`)
    return newTrade
  } catch (error) {
    console.error('添加待匹配交易失败:', error)
    return null
  }
}

/**
 * 清空所有交易记录
 */
export function clearTradeRecords() {
  try {
    const userTradeKey = getUserTradeRecordsKey()
    const userPendingKey = getUserPendingTradesKey()
    localStorage.removeItem(userTradeKey)
    localStorage.removeItem(userPendingKey)
    console.log(`清空用户 ${getCurrentUsername()} 的交易记录`)
  } catch (error) {
    console.error('清空交易记录失败:', error)
  }
}

/**
 * 模拟交易执行（将待匹配交易转换为已完成交易）
 * 在实际应用中，这个过程应该由后端完成
 */
export function simulateTradeExecution() {
  try {
    const pendingTrades = getPendingTrades()
    if (pendingTrades.length === 0) return
    
    // 模拟：将最早的待匹配交易标记为已执行
    const executedTrade = pendingTrades.pop()
    
    // 更新待匹配交易列表
    const userPendingKey = getUserPendingTradesKey()
    localStorage.setItem(userPendingKey, JSON.stringify(pendingTrades))
    
    // 添加到已完成交易记录
    addTradeRecord(executedTrade)
    
    return executedTrade
  } catch (error) {
    console.error('模拟交易执行失败:', error)
    return null
  }
}

/**
 * 获取默认交易记录
 */
function getDefaultTradeRecords() {
  return [
    {
      id: 1,
      date: '2024-03-15',
      stockName: '阿里巴巴',
      stockCode: '9988.HK',
      type: '买入',
      price: 88.50,
      quantity: 100,
      amount: 8850.00,
      timestamp: new Date('2024-03-15').getTime()
    },
    {
      id: 2,
      date: '2024-03-14',
      stockName: '腾讯控股',
      stockCode: '0700.HK',
      type: '卖出',
      price: 370.00,
      quantity: 20,
      amount: 7400.00,
      timestamp: new Date('2024-03-14').getTime()
    },
    {
      id: 3,
      date: '2024-03-11',
      stockName: '小米集团',
      stockCode: '1810.HK',
      type: '买入',
      price: 12.80,
      quantity: 500,
      amount: 6400.00,
      timestamp: new Date('2024-03-11').getTime()
    },
    {
      id: 4,
      date: '2024-03-10',
      stockName: '腾讯控股',
      stockCode: '0700.HK',
      type: '买入',
      price: 365.00,
      quantity: 50,
      amount: 18250.00,
      timestamp: new Date('2024-03-10').getTime()
    }
  ]
}

/**
 * 初始化默认交易记录（仅在没有记录时执行）
 */
export function initializeDefaultRecords() {
  const userKey = getUserTradeRecordsKey()
  const existingRecords = localStorage.getItem(userKey)
  if (!existingRecords) {
    const defaultRecords = getDefaultTradeRecords()
    localStorage.setItem(userKey, JSON.stringify(defaultRecords))
    console.log(`为用户 ${getCurrentUsername()} 初始化默认交易记录`)
  }
}
