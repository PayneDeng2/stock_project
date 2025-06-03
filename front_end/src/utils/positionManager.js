/**
 * 持仓管理工具
 * 统一管理所有持仓相关的操作，避免各组件重复定义
 * 支持多用户数据隔离
 */

import { getAllAvailableStocks, getStockRealtimeData } from './stockDataService'

const BASE_POSITIONS_KEY = 'positions'

/**
 * 获取当前登录用户名
 */
function getCurrentUsername() {
  return sessionStorage.getItem('loggedInUserDemo') || 'default'
}

/**
 * 获取用户专属的存储key
 */
function getUserPositionsKey() {
  const username = getCurrentUsername()
  return `${BASE_POSITIONS_KEY}_${username}`
}

/**
 * 为新用户生成初始持仓（每只股票30股）
 */
function generateInitialPositions() {
  console.log('开始生成初始持仓...')
  const allStocks = getAllAvailableStocks()
  const initialPositions = []
  
  allStocks.forEach(stock => {
    // 获取股票当前价格作为初始成本价
    const stockData = getStockRealtimeData(stock.code)
    const avgPrice = parseFloat(stockData.currentPrice)
    
    const position = {
      stockName: stock.name,
      stockCode: stock.code,
      quantity: 30, // 每只股票30股
      avgPrice: avgPrice,
      currentPrice: avgPrice,
      profitLoss: 0 // 初始盈亏为0
    }
    
    initialPositions.push(position)
    console.log(`初始化持仓: ${stock.name}(${stock.code}) - 30股 @ ¥${avgPrice}`)
  })
  
  console.log(`初始持仓生成完成，共 ${initialPositions.length} 只股票`)
  return initialPositions
}

/**
 * 获取持仓数据
 */
export function getPositions() {
  try {
    const userKey = getUserPositionsKey()
    const positions = localStorage.getItem(userKey)
    console.log(`获取用户 ${getCurrentUsername()} 的持仓数据，key: ${userKey}`)
    return positions ? JSON.parse(positions) : []
  } catch (error) {
    console.error('获取持仓数据失败:', error)
    return []
  }
}

/**
 * 保存持仓数据
 */
export function savePositions(positions) {
  try {
    const userKey = getUserPositionsKey()
    localStorage.setItem(userKey, JSON.stringify(positions))
    console.log(`保存用户 ${getCurrentUsername()} 的持仓数据，key: ${userKey}`)
    return true
  } catch (error) {
    console.error('保存持仓数据失败:', error)
    return false
  }
}

/**
 * 根据交易更新持仓
 * @param {string} stockCode - 股票代码
 * @param {string} stockName - 股票名称
 * @param {string} type - 交易类型 ('买入' 或 '卖出')
 * @param {number} quantity - 交易数量
 * @param {number} price - 交易价格
 */
export function updatePositionByTrade(stockCode, stockName, type, quantity, price) {
  console.log('开始更新持仓:', { stockCode, stockName, type, quantity, price })
  
  const positions = getPositions()
  console.log('当前持仓列表:', positions)
  
  // 查找现有持仓
  let existingPosition = positions.find(pos => pos.stockCode === stockCode)
  console.log('找到的现有持仓:', existingPosition)
  
  if (type === '买入') {
    console.log('执行买入操作')
    if (existingPosition) {
      // 更新现有持仓 - 计算加权平均价格
      const oldQuantity = existingPosition.quantity
      const oldAvgPrice = existingPosition.avgPrice
      const totalQuantity = oldQuantity + quantity
      const totalCost = (oldQuantity * oldAvgPrice) + (quantity * price)
      
      existingPosition.quantity = totalQuantity
      existingPosition.avgPrice = parseFloat((totalCost / totalQuantity).toFixed(2))
      // 更新当前价格为最新交易价格
      existingPosition.currentPrice = price
      // 重新计算盈亏
      existingPosition.profitLoss = parseFloat((((existingPosition.currentPrice - existingPosition.avgPrice) / existingPosition.avgPrice) * 100).toFixed(2))
      
      console.log('更新现有持仓:', {
        oldQuantity,
        newQuantity: existingPosition.quantity,
        oldAvgPrice,
        newAvgPrice: existingPosition.avgPrice,
        currentPrice: existingPosition.currentPrice,
        profitLoss: existingPosition.profitLoss
      })
    } else {
      // 新增持仓
      const newPosition = {
        stockName: stockName,
        stockCode: stockCode,
        quantity: quantity,
        avgPrice: price,
        currentPrice: price,
        profitLoss: 0
      }
      positions.push(newPosition)
      console.log('新增持仓:', newPosition)
    }
  } else if (type === '卖出') {
    console.log('执行卖出操作')
    if (existingPosition) {
      if (existingPosition.quantity >= quantity) {
        const oldQuantity = existingPosition.quantity
        existingPosition.quantity -= quantity
        
        console.log(`卖出成功: 原数量${oldQuantity} - 卖出${quantity} = 剩余${existingPosition.quantity}`)
        
        // 如果卖完了，从持仓中移除
        if (existingPosition.quantity === 0) {
          const index = positions.findIndex(pos => pos.stockCode === stockCode)
          positions.splice(index, 1)
          console.log('持仓已清空，从列表中移除')
        } else {
          // 更新当前价格为最新交易价格
          existingPosition.currentPrice = price
          // 重新计算盈亏
          existingPosition.profitLoss = parseFloat((((existingPosition.currentPrice - existingPosition.avgPrice) / existingPosition.avgPrice) * 100).toFixed(2))
        }
      } else {
        console.error('卖出失败: 持仓数量不足', {
          requested: quantity,
          available: existingPosition.quantity
        })
        throw new Error(`持仓数量不足，当前持有${existingPosition.quantity}股，无法卖出${quantity}股`)
      }
    } else {
      console.error('卖出失败: 未找到持仓')
      throw new Error('您没有持有该股票，无法卖出')
    }
  } else {
    console.error('未知的交易类型:', type)
    throw new Error('未知的交易类型')
  }
  
  console.log('最终持仓列表:', positions)
  
  // 保存更新后的持仓数据
  const saved = savePositions(positions)
  if (saved) {
    console.log('持仓数据已成功保存')
  } else {
    console.error('持仓数据保存失败')
  }
  
  return positions
}

/**
 * 获取特定股票的持仓信息
 */
export function getPositionByStockCode(stockCode) {
  const positions = getPositions()
  return positions.find(pos => pos.stockCode === stockCode)
}

/**
 * 检查是否有足够的持仓进行卖出
 */
export function checkSellAvailability(stockCode, quantity) {
  const position = getPositionByStockCode(stockCode)
  if (!position) {
    return { available: false, message: '您没有持有该股票' }
  }
  if (position.quantity < quantity) {
    return { 
      available: false, 
      message: `持仓数量不足，当前持有${position.quantity}股，无法卖出${quantity}股`
    }
  }
  return { available: true }
}

/**
 * 为新用户初始化持仓数据（每只股票30股）
 * 只在用户注册时调用，不会重复初始化
 */
export function initializeNewUserPositions() {
  const userKey = getUserPositionsKey()
  
  // 检查是否已经有持仓数据
  if (localStorage.getItem(userKey)) {
    console.log(`用户 ${getCurrentUsername()} 已有持仓数据，跳过初始化`)
    return false
  }
  
  // 生成初始持仓并保存
  const initialPositions = generateInitialPositions()
  const saved = savePositions(initialPositions)
  
  if (saved) {
    console.log(`用户 ${getCurrentUsername()} 初始持仓设置完成`)
    return true
  } else {
    console.error(`用户 ${getCurrentUsername()} 初始持仓设置失败`)
    return false
  }
}

/**
 * 检查并初始化持仓数据（已登录用户，不会重复初始化）
 * 这个函数不会为用户创建初始持仓，只用于兼容性
 */
export function initializeDefaultPositions() {
  const userKey = getUserPositionsKey()
  
  // 如果没有持仓数据，什么都不做（不再自动创建默认持仓）
  if (!localStorage.getItem(userKey)) {
    console.log(`用户 ${getCurrentUsername()} 没有持仓数据，等待用户交易或手动初始化`)
  }
}

/**
 * 清空所有持仓数据
 */
export function clearAllPositions() {
  try {
    localStorage.removeItem(getUserPositionsKey())
    console.log('已清空所有持仓数据')
    return true
  } catch (error) {
    console.error('清空持仓数据失败:', error)
    return false
  }
} 