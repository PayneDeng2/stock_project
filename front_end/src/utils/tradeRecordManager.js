/**
 * 交易记录管理工具
 * 支持多用户数据隔离
 */

const BASE_TRADE_RECORDS_KEY = 'tradeRecords'

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
 * 获取交易记录
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
 * 保存交易记录
 */
export function saveTradeRecords(records) {
  try {
    const userKey = getUserTradeRecordsKey()
    localStorage.setItem(userKey, JSON.stringify(records))
    console.log(`保存用户 ${getCurrentUsername()} 的交易记录，key: ${userKey}`)
    return true
  } catch (error) {
    console.error('保存交易记录失败:', error)
    return false
  }
}

/**
 * 添加交易记录
 */
export function addTradeRecord(record) {
  try {
    const records = getTradeRecords()
    const newRecord = {
      id: Date.now(),
      date: new Date().toLocaleDateString(),
      stockName: record.stockName,
      stockCode: record.stockCode,
      type: record.type,
      price: `¥${record.price}`,
      quantity: record.quantity,
      amount: `¥${record.totalPrice}`,
      timestamp: Date.now()
    }
    records.unshift(newRecord)
    
    // 限制记录数量，保留最近100条
    if (records.length > 100) {
      records.splice(100)
    }
    
    saveTradeRecords(records)
    return newRecord
  } catch (error) {
    console.error('添加交易记录失败:', error)
    return null
  }
}

/**
 * 初始化默认交易记录（仅在没有记录时执行）
 */
export function initializeDefaultTradeRecords() {
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
    saveTradeRecords(defaultRecords)
    console.log('已初始化默认交易记录')
  }
}

/**
 * 清空交易记录
 */
export function clearTradeRecords() {
  try {
    localStorage.removeItem(getUserTradeRecordsKey())
    console.log('已清空交易记录')
    return true
  } catch (error) {
    console.error('清空交易记录失败:', error)
    return false
  }
} 