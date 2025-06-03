/**
 * 资金管理工具
 * 统一管理可用资金，支持多用户数据隔离
 */

const BASE_FUNDS_KEY = 'availableFunds'

/**
 * 获取当前登录用户名
 */
function getCurrentUsername() {
  return sessionStorage.getItem('loggedInUserDemo') || 'default'
}

/**
 * 获取用户专属的资金存储key
 */
function getUserFundsKey() {
  const username = getCurrentUsername()
  return `${BASE_FUNDS_KEY}_${username}`
}

/**
 * 获取可用资金
 */
export function getAvailableFunds() {
  try {
    const userKey = getUserFundsKey()
    const funds = localStorage.getItem(userKey)
    console.log(`获取用户 ${getCurrentUsername()} 的可用资金，key: ${userKey}`)
    return funds ? parseFloat(funds) : 50000.00 // 默认5万元初始资金
  } catch (error) {
    console.error('获取可用资金失败:', error)
    return 50000.00
  }
}

/**
 * 保存可用资金
 */
export function saveAvailableFunds(amount) {
  try {
    const userKey = getUserFundsKey()
    localStorage.setItem(userKey, amount.toString())
    console.log(`保存用户 ${getCurrentUsername()} 的可用资金: ${amount}，key: ${userKey}`)
    return true
  } catch (error) {
    console.error('保存可用资金失败:', error)
    return false
  }
}

/**
 * 更新可用资金（买入股票时减少资金）
 * @param {number} amount - 减少的金额
 */
export function decreaseFunds(amount) {
  try {
    const currentFunds = getAvailableFunds()
    if (currentFunds < amount) {
      throw new Error('可用资金不足')
    }
    const newFunds = currentFunds - amount
    saveAvailableFunds(newFunds)
    console.log(`资金减少: ${amount}，当前可用资金: ${newFunds}`)
    return newFunds
  } catch (error) {
    console.error('减少资金失败:', error)
    throw error
  }
}

/**
 * 增加可用资金（卖出股票时增加资金）
 * @param {number} amount - 增加的金额
 */
export function increaseFunds(amount) {
  try {
    const currentFunds = getAvailableFunds()
    const newFunds = currentFunds + amount
    saveAvailableFunds(newFunds)
    console.log(`资金增加: ${amount}，当前可用资金: ${newFunds}`)
    return newFunds
  } catch (error) {
    console.error('增加资金失败:', error)
    throw error
  }
}

/**
 * 充值资金
 * @param {number} amount - 充值金额
 */
export function depositFunds(amount) {
  try {
    return increaseFunds(amount)
  } catch (error) {
    console.error('充值失败:', error)
    throw error
  }
}

/**
 * 提现资金
 * @param {number} amount - 提现金额
 */
export function withdrawFunds(amount) {
  try {
    return decreaseFunds(amount)
  } catch (error) {
    console.error('提现失败:', error)
    throw error
  }
}

/**
 * 检查资金是否足够
 * @param {number} amount - 需要的金额
 */
export function checkFundsAvailability(amount) {
  const currentFunds = getAvailableFunds()
  return {
    available: currentFunds >= amount,
    currentFunds,
    requiredFunds: amount,
    shortfall: amount > currentFunds ? amount - currentFunds : 0
  }
}

/**
 * 初始化用户资金（如果没有记录）
 */
export function initializeUserFunds() {
  const userKey = getUserFundsKey()
  const existingFunds = localStorage.getItem(userKey)
  if (!existingFunds) {
    saveAvailableFunds(50000.00) // 初始资金5万元
    console.log(`为用户 ${getCurrentUsername()} 初始化资金: 50000.00`)
  }
}

/**
 * 处理交易资金变化
 * @param {string} tradeType - 交易类型：'买入' 或 '卖出'
 * @param {number} amount - 交易金额
 */
export function handleTradeFunds(tradeType, amount) {
  try {
    if (tradeType === '买入') {
      return decreaseFunds(amount)
    } else if (tradeType === '卖出') {
      return increaseFunds(amount)
    } else {
      throw new Error('未知的交易类型')
    }
  } catch (error) {
    console.error('处理交易资金变化失败:', error)
    throw error
  }
} 