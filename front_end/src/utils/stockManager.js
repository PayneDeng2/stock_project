/**
 * 自选股和股票提醒管理工具
 * 用于管理用户的自选股列表和价格提醒
 * 支持多用户数据隔离
 */

const BASE_WATCHLIST_KEY = 'stock_watchlist'
const BASE_ALERTS_KEY = 'stock_alerts'

/**
 * 获取当前登录用户名
 */
function getCurrentUsername() {
  return sessionStorage.getItem('loggedInUserDemo') || 'default'
}

/**
 * 获取用户专属的自选股存储key
 */
function getUserWatchlistKey() {
  const username = getCurrentUsername()
  return `${BASE_WATCHLIST_KEY}_${username}`
}

/**
 * 获取用户专属的提醒存储key
 */
function getUserAlertsKey() {
  const username = getCurrentUsername()
  return `${BASE_ALERTS_KEY}_${username}`
}

/**
 * 获取自选股列表
 */
export function getWatchlist() {
  try {
    const userKey = getUserWatchlistKey()
    const watchlist = localStorage.getItem(userKey)
    console.log(`获取用户 ${getCurrentUsername()} 的自选股列表，key: ${userKey}`)
    return watchlist ? JSON.parse(watchlist) : []
  } catch (error) {
    console.error('获取自选股列表失败:', error)
    return []
  }
}

/**
 * 添加股票到自选
 */
export function addToWatchlist(stock) {
  try {
    const watchlist = getWatchlist()
    
    // 检查是否已存在
    const existingIndex = watchlist.findIndex(item => item.code === stock.code)
    if (existingIndex !== -1) {
      // 已存在则更新信息
      watchlist[existingIndex] = {
        ...watchlist[existingIndex],
        ...stock,
        addTime: watchlist[existingIndex].addTime // 保持原添加时间
      }
    } else {
      // 新增
      const newStock = {
        ...stock,
        addTime: new Date().getTime(),
        id: Date.now() + Math.random()
      }
      watchlist.unshift(newStock)
    }
    
    const userKey = getUserWatchlistKey()
    localStorage.setItem(userKey, JSON.stringify(watchlist))
    console.log(`保存用户 ${getCurrentUsername()} 的自选股列表，key: ${userKey}`)
    return true
  } catch (error) {
    console.error('添加自选股失败:', error)
    return false
  }
}

/**
 * 从自选中移除股票
 */
export function removeFromWatchlist(stockCode) {
  try {
    const watchlist = getWatchlist()
    const filteredList = watchlist.filter(item => item.code !== stockCode)
    const userKey = getUserWatchlistKey()
    localStorage.setItem(userKey, JSON.stringify(filteredList))
    console.log(`从用户 ${getCurrentUsername()} 的自选股中移除 ${stockCode}`)
    return true
  } catch (error) {
    console.error('移除自选股失败:', error)
    return false
  }
}

/**
 * 检查股票是否在自选中
 */
export function isInWatchlist(stockCode) {
  const watchlist = getWatchlist()
  return watchlist.some(item => item.code === stockCode)
}

/**
 * 获取股票提醒列表
 */
export function getAlerts() {
  try {
    const userKey = getUserAlertsKey()
    const alerts = localStorage.getItem(userKey)
    console.log(`获取用户 ${getCurrentUsername()} 的股票提醒，key: ${userKey}`)
    return alerts ? JSON.parse(alerts) : []
  } catch (error) {
    console.error('获取提醒列表失败:', error)
    return []
  }
}

/**
 * 添加股票提醒
 */
export function addAlert(alert) {
  try {
    const alerts = getAlerts()
    const newAlert = {
      ...alert,
      id: Date.now() + Math.random(),
      createTime: new Date().getTime(),
      isActive: true,
      triggered: false
    }
    
    alerts.unshift(newAlert)
    
    // 限制提醒数量
    if (alerts.length > 100) {
      alerts.splice(100)
    }
    
    const userKey = getUserAlertsKey()
    localStorage.setItem(userKey, JSON.stringify(alerts))
    console.log(`保存用户 ${getCurrentUsername()} 的股票提醒，key: ${userKey}`)
    return newAlert
  } catch (error) {
    console.error('添加提醒失败:', error)
    return null
  }
}

/**
 * 移除提醒
 */
export function removeAlert(alertId) {
  try {
    const alerts = getAlerts()
    const filteredAlerts = alerts.filter(alert => alert.id !== alertId)
    const userKey = getUserAlertsKey()
    localStorage.setItem(userKey, JSON.stringify(filteredAlerts))
    console.log(`移除用户 ${getCurrentUsername()} 的提醒 ${alertId}`)
    return true
  } catch (error) {
    console.error('移除提醒失败:', error)
    return false
  }
}

/**
 * 更新提醒状态
 */
export function updateAlert(alertId, updates) {
  try {
    const alerts = getAlerts()
    const alertIndex = alerts.findIndex(alert => alert.id === alertId)
    
    if (alertIndex !== -1) {
      alerts[alertIndex] = { ...alerts[alertIndex], ...updates }
      const userKey = getUserAlertsKey()
      localStorage.setItem(userKey, JSON.stringify(alerts))
      console.log(`更新用户 ${getCurrentUsername()} 的提醒 ${alertId}`)
      return true
    }
    return false
  } catch (error) {
    console.error('更新提醒失败:', error)
    return false
  }
}

/**
 * 获取股票的活跃提醒
 */
export function getStockAlerts(stockCode) {
  const alerts = getAlerts()
  return alerts.filter(alert => 
    alert.stockCode === stockCode && 
    alert.isActive && 
    !alert.triggered
  )
}

/**
 * 模拟检查价格提醒
 * 在实际应用中，这应该由后端定时任务完成
 */
export function checkPriceAlerts(stockData) {
  try {
    const alerts = getAlerts()
    const triggeredAlerts = []
    
    alerts.forEach(alert => {
      if (!alert.isActive || alert.triggered || alert.stockCode !== stockData.code) {
        return
      }
      
      const currentPrice = parseFloat(stockData.currentPrice)
      const targetPrice = parseFloat(alert.targetPrice)
      
      let shouldTrigger = false
      
      switch (alert.condition) {
        case 'above':
          shouldTrigger = currentPrice >= targetPrice
          break
        case 'below':
          shouldTrigger = currentPrice <= targetPrice
          break
        case 'equal':
          shouldTrigger = Math.abs(currentPrice - targetPrice) < 0.01
          break
      }
      
      if (shouldTrigger) {
        alert.triggered = true
        alert.triggerTime = new Date().getTime()
        triggeredAlerts.push(alert)
      }
    })
    
    if (triggeredAlerts.length > 0) {
      const userKey = getUserAlertsKey()
      localStorage.setItem(userKey, JSON.stringify(alerts))
    }
    
    return triggeredAlerts
  } catch (error) {
    console.error('检查价格提醒失败:', error)
    return []
  }
}

/**
 * 清空用户所有数据
 */
export function clearAllData() {
  try {
    const userWatchlistKey = getUserWatchlistKey()
    const userAlertsKey = getUserAlertsKey()
    localStorage.removeItem(userWatchlistKey)
    localStorage.removeItem(userAlertsKey)
    console.log(`清空用户 ${getCurrentUsername()} 的所有股票数据`)
  } catch (error) {
    console.error('清空数据失败:', error)
  }
}

/**
 * 初始化默认自选股列表（仅在没有数据时执行）
 */
export function initializeDefaultWatchlist() {
  const userKey = getUserWatchlistKey()
  const existingWatchlist = localStorage.getItem(userKey)
  if (!existingWatchlist) {
    // 初始化为空数组，不添加默认股票
    localStorage.setItem(userKey, JSON.stringify([]))
    console.log(`为用户 ${getCurrentUsername()} 初始化空的自选股列表`)
  }
}
