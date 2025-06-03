/**
 * 订单管理器
 * 实现订单撮合逻辑，支持多用户交易
 */

const ORDERS_KEY = 'trading_orders' // 全局订单簿
const TRADES_KEY = 'trading_trades' // 全局成交记录

import { handleTradeFunds } from './fundManager'
import { updatePositionByTrade } from './positionManager'
import { addTradeRecord } from './tradeManager'

/**
 * 订单状态
 */
export const ORDER_STATUS = {
  PENDING: 'PENDING',     // 待撮合
  PARTIAL: 'PARTIAL',     // 部分成交
  FILLED: 'FILLED',       // 完全成交
  CANCELLED: 'CANCELLED'  // 已取消
}

/**
 * 订单类型
 */
export const ORDER_TYPE = {
  BUY: 'BUY',   // 买单
  SELL: 'SELL'  // 卖单
}

/**
 * 获取当前用户名
 */
function getCurrentUsername() {
  return sessionStorage.getItem('loggedInUserDemo') || 'default'
}

/**
 * 获取全局订单簿
 * 订单簿结构：{ stockCode: { buyOrders: [], sellOrders: [] } }
 */
export function getOrderBook() {
  try {
    const orderBook = localStorage.getItem(ORDERS_KEY)
    return orderBook ? JSON.parse(orderBook) : {}
  } catch (error) {
    console.error('获取订单簿失败:', error)
    return {}
  }
}

/**
 * 保存订单簿
 */
function saveOrderBook(orderBook) {
  try {
    localStorage.setItem(ORDERS_KEY, JSON.stringify(orderBook))
    return true
  } catch (error) {
    console.error('保存订单簿失败:', error)
    return false
  }
}

/**
 * 获取全局成交记录
 */
export function getAllTrades() {
  try {
    const trades = localStorage.getItem(TRADES_KEY)
    return trades ? JSON.parse(trades) : []
  } catch (error) {
    console.error('获取成交记录失败:', error)
    return []
  }
}

/**
 * 保存成交记录
 */
function saveTradeRecord(trade) {
  try {
    const trades = getAllTrades()
    trades.unshift(trade)
    
    // 限制记录数量，保留最近500条
    if (trades.length > 500) {
      trades.splice(500)
    }
    
    localStorage.setItem(TRADES_KEY, JSON.stringify(trades))
    return true
  } catch (error) {
    console.error('保存成交记录失败:', error)
    return false
  }
}

/**
 * 创建订单
 */
export function createOrder(orderData) {
  const order = {
    id: generateOrderId(),
    userId: getCurrentUsername(),
    stockCode: orderData.stockCode,
    stockName: orderData.stockName,
    type: orderData.type, // 'BUY' | 'SELL'
    price: parseFloat(orderData.price),
    quantity: parseInt(orderData.quantity),
    remainingQuantity: parseInt(orderData.quantity),
    timestamp: Date.now(),
    status: ORDER_STATUS.PENDING
  }
  
  console.log('创建订单:', order)
  return order
}

/**
 * 生成订单ID
 */
function generateOrderId() {
  return `order_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
}

/**
 * 提交订单并尝试撮合
 */
export function submitOrder(orderData) {
  try {
    // 创建订单
    const order = createOrder(orderData)
    
    // 尝试撮合
    const matchResult = matchOrder(order)
    
    console.log('撮合结果:', matchResult)
    
    return {
      success: true,
      order: order,
      trades: matchResult.trades,
      message: matchResult.message
    }
  } catch (error) {
    console.error('提交订单失败:', error)
    return {
      success: false,
      error: error.message
    }
  }
}

/**
 * 订单撮合核心逻辑
 */
function matchOrder(newOrder) {
  const orderBook = getOrderBook()
  const stockCode = newOrder.stockCode
  const trades = []
  
  // 确保该股票的订单簿存在
  if (!orderBook[stockCode]) {
    orderBook[stockCode] = { buyOrders: [], sellOrders: [] }
  }
  
  if (newOrder.type === ORDER_TYPE.BUY) {
    // 处理买单：与最低价卖单撮合
    matchBuyOrder(newOrder, orderBook[stockCode], trades)
  } else {
    // 处理卖单：与最高价买单撮合
    matchSellOrder(newOrder, orderBook[stockCode], trades)
  }
  
  // 如果订单还有剩余，加入订单簿
  if (newOrder.remainingQuantity > 0) {
    addOrderToBook(newOrder, orderBook[stockCode])
  }
  
  // 保存更新后的订单簿
  saveOrderBook(orderBook)
  
  // 处理成交记录和账户更新
  processTrades(trades)
  
  const message = generateMatchMessage(newOrder, trades)
  
  return { trades, message }
}

/**
 * 撮合买单
 */
function matchBuyOrder(buyOrder, stockOrderBook, trades) {
  // 按价格升序排序卖单（价格低的优先）
  stockOrderBook.sellOrders.sort((a, b) => {
    if (a.price !== b.price) return a.price - b.price
    return a.timestamp - b.timestamp // 价格相同时按时间优先
  })
  
  let i = 0
  while (i < stockOrderBook.sellOrders.length && buyOrder.remainingQuantity > 0) {
    const sellOrder = stockOrderBook.sellOrders[i]
    
    // 检查是否可以撮合
    if (buyOrder.price >= sellOrder.price) {
      const matchQuantity = Math.min(buyOrder.remainingQuantity, sellOrder.remainingQuantity)
      const matchPrice = sellOrder.price // 以卖方价格成交
      
      // 创建成交记录
      const trade = createTrade(buyOrder, sellOrder, matchPrice, matchQuantity)
      trades.push(trade)
      
      // 更新订单数量
      buyOrder.remainingQuantity -= matchQuantity
      sellOrder.remainingQuantity -= matchQuantity
      
      // 更新订单状态
      if (buyOrder.remainingQuantity === 0) {
        buyOrder.status = ORDER_STATUS.FILLED
      } else {
        buyOrder.status = ORDER_STATUS.PARTIAL
      }
      
      if (sellOrder.remainingQuantity === 0) {
        sellOrder.status = ORDER_STATUS.FILLED
        stockOrderBook.sellOrders.splice(i, 1) // 移除完全成交的卖单
      } else {
        sellOrder.status = ORDER_STATUS.PARTIAL
        i++
      }
    } else {
      break // 价格不匹配，停止撮合
    }
  }
}

/**
 * 撮合卖单
 */
function matchSellOrder(sellOrder, stockOrderBook, trades) {
  // 按价格降序排序买单（价格高的优先）
  stockOrderBook.buyOrders.sort((a, b) => {
    if (a.price !== b.price) return b.price - a.price
    return a.timestamp - b.timestamp // 价格相同时按时间优先
  })
  
  let i = 0
  while (i < stockOrderBook.buyOrders.length && sellOrder.remainingQuantity > 0) {
    const buyOrder = stockOrderBook.buyOrders[i]
    
    // 检查是否可以撮合
    if (buyOrder.price >= sellOrder.price) {
      const matchQuantity = Math.min(sellOrder.remainingQuantity, buyOrder.remainingQuantity)
      const matchPrice = sellOrder.price // 以卖方价格成交
      
      // 创建成交记录
      const trade = createTrade(buyOrder, sellOrder, matchPrice, matchQuantity)
      trades.push(trade)
      
      // 更新订单数量
      sellOrder.remainingQuantity -= matchQuantity
      buyOrder.remainingQuantity -= matchQuantity
      
      // 更新订单状态
      if (sellOrder.remainingQuantity === 0) {
        sellOrder.status = ORDER_STATUS.FILLED
      } else {
        sellOrder.status = ORDER_STATUS.PARTIAL
      }
      
      if (buyOrder.remainingQuantity === 0) {
        buyOrder.status = ORDER_STATUS.FILLED
        stockOrderBook.buyOrders.splice(i, 1) // 移除完全成交的买单
      } else {
        buyOrder.status = ORDER_STATUS.PARTIAL
        i++
      }
    } else {
      break // 价格不匹配，停止撮合
    }
  }
}

/**
 * 将订单加入订单簿
 */
function addOrderToBook(order, stockOrderBook) {
  if (order.type === ORDER_TYPE.BUY) {
    stockOrderBook.buyOrders.push(order)
  } else {
    stockOrderBook.sellOrders.push(order)
  }
}

/**
 * 创建成交记录
 */
function createTrade(buyOrder, sellOrder, price, quantity) {
  const trade = {
    id: generateTradeId(),
    stockCode: buyOrder.stockCode,
    stockName: buyOrder.stockName,
    buyOrderId: buyOrder.id,
    sellOrderId: sellOrder.id,
    buyUserId: buyOrder.userId,
    sellUserId: sellOrder.userId,
    price: price,
    quantity: quantity,
    amount: price * quantity,
    timestamp: Date.now()
  }
  
  console.log('创建成交记录:', trade)
  return trade
}

/**
 * 生成成交ID
 */
function generateTradeId() {
  return `trade_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
}

/**
 * 处理成交记录 - 更新用户账户
 */
function processTrades(trades) {
  trades.forEach(trade => {
    try {
      // 保存全局成交记录
      saveTradeRecord(trade)
      
      // 更新买方账户
      processUserTrade(trade.buyUserId, trade, 'BUY')
      
      // 更新卖方账户
      processUserTrade(trade.sellUserId, trade, 'SELL')
      
    } catch (error) {
      console.error('处理成交记录失败:', error)
    }
  })
}

/**
 * 处理单个用户的成交记录
 */
function processUserTrade(userId, trade, side) {
  // 临时切换到目标用户进行数据更新
  const currentUser = getCurrentUsername()
  sessionStorage.setItem('loggedInUserDemo', userId)
  
  try {
    const type = side === 'BUY' ? '买入' : '卖出'
    
    // 更新资金
    handleTradeFunds(type, trade.amount)
    
    // 更新持仓
    updatePositionByTrade(
      trade.stockCode,
      trade.stockName,
      type,
      trade.quantity,
      trade.price
    )
    
    // 添加交易记录
    addTradeRecord({
      stockCode: trade.stockCode,
      stockName: trade.stockName,
      type: type,
      price: trade.price.toFixed(2),
      quantity: trade.quantity,
      totalPrice: trade.amount.toFixed(2)
    })
    
    console.log(`用户 ${userId} 的${type}交易已处理`)
    
  } catch (error) {
    console.error(`处理用户 ${userId} 的交易失败:`, error)
  } finally {
    // 恢复原用户
    sessionStorage.setItem('loggedInUserDemo', currentUser)
  }
}

/**
 * 生成撮合结果消息
 */
function generateMatchMessage(order, trades) {
  if (trades.length === 0) {
    return `订单已提交，等待撮合（价格: ¥${order.price}，数量: ${order.quantity}股）`
  }
  
  const totalMatched = trades.reduce((sum, trade) => sum + trade.quantity, 0)
  const avgPrice = trades.reduce((sum, trade) => sum + trade.price * trade.quantity, 0) / totalMatched
  
  if (order.remainingQuantity === 0) {
    return `订单完全撮合成功！成交 ${totalMatched} 股，平均价格 ¥${avgPrice.toFixed(2)}`
  } else {
    return `订单部分撮合成功！成交 ${totalMatched} 股，剩余 ${order.remainingQuantity} 股继续等待撮合`
  }
}

/**
 * 获取用户的活跃订单
 */
export function getUserActiveOrders(userId = null) {
  const targetUser = userId || getCurrentUsername()
  const orderBook = getOrderBook()
  const userOrders = []
  
  Object.keys(orderBook).forEach(stockCode => {
    const stock = orderBook[stockCode]
    
    // 买单
    stock.buyOrders.forEach(order => {
      if (order.userId === targetUser && order.status !== ORDER_STATUS.FILLED) {
        userOrders.push(order)
      }
    })
    
    // 卖单
    stock.sellOrders.forEach(order => {
      if (order.userId === targetUser && order.status !== ORDER_STATUS.FILLED) {
        userOrders.push(order)
      }
    })
  })
  
  return userOrders.sort((a, b) => b.timestamp - a.timestamp)
}

/**
 * 取消订单
 */
export function cancelOrder(orderId) {
  try {
    const orderBook = getOrderBook()
    let found = false
    
    Object.keys(orderBook).forEach(stockCode => {
      const stock = orderBook[stockCode]
      
      // 在买单中查找
      const buyIndex = stock.buyOrders.findIndex(order => order.id === orderId)
      if (buyIndex !== -1) {
        stock.buyOrders[buyIndex].status = ORDER_STATUS.CANCELLED
        stock.buyOrders.splice(buyIndex, 1)
        found = true
      }
      
      // 在卖单中查找
      const sellIndex = stock.sellOrders.findIndex(order => order.id === orderId)
      if (sellIndex !== -1) {
        stock.sellOrders[sellIndex].status = ORDER_STATUS.CANCELLED
        stock.sellOrders.splice(sellIndex, 1)
        found = true
      }
    })
    
    if (found) {
      saveOrderBook(orderBook)
      return { success: true, message: '订单已取消' }
    } else {
      return { success: false, message: '订单不存在或已处理' }
    }
  } catch (error) {
    console.error('取消订单失败:', error)
    return { success: false, message: '取消订单失败' }
  }
}

/**
 * 获取股票的当前最优报价
 */
export function getBestQuote(stockCode) {
  const orderBook = getOrderBook()
  const stock = orderBook[stockCode]
  
  if (!stock) {
    return { bid: null, ask: null }
  }
  
  // 最高买价
  const bestBuyOrder = stock.buyOrders
    .filter(order => order.status === ORDER_STATUS.PENDING)
    .sort((a, b) => b.price - a.price)[0]
  
  // 最低卖价
  const bestSellOrder = stock.sellOrders
    .filter(order => order.status === ORDER_STATUS.PENDING)
    .sort((a, b) => a.price - b.price)[0]
  
  return {
    bid: bestBuyOrder ? bestBuyOrder.price : null,
    ask: bestSellOrder ? bestSellOrder.price : null
  }
} 