/**
 * 股票数据模拟服务
 * 提供相对稳定的股票数据，避免每次刷新都随机变化
 */

// 固定的股票基础数据
const STOCK_BASE_DATA = {
  '600519': { 
    name: '贵州茅台', 
    basePrice: 1580.00,
    sector: '白酒',
    market: 'SH'
  },
  '000001': { 
    name: '平安银行', 
    basePrice: 12.50,
    sector: '银行',
    market: 'SZ'
  },
  '600036': { 
    name: '招商银行', 
    basePrice: 35.80,
    sector: '银行',
    market: 'SH'
  },
  '601318': { 
    name: '中国平安', 
    basePrice: 45.20,
    sector: '保险',
    market: 'SH'
  },
  '000002': { 
    name: '万科A', 
    basePrice: 16.30,
    sector: '地产',
    market: 'SZ'
  },
  '601857': { 
    name: '中国石油', 
    basePrice: 7.20,
    sector: '石油',
    market: 'SH'
  },
  '9988': { 
    name: '阿里巴巴', 
    basePrice: 76.50,
    sector: '互联网',
    market: 'HK'
  },
  '0700': { 
    name: '腾讯控股', 
    basePrice: 315.20,
    sector: '互联网',
    market: 'HK'
  },
  '601398': { 
    name: '中国工商银行', 
    basePrice: 4.85,
    sector: '银行',
    market: 'SH'
  },
  '601939': { 
    name: '中国建设银行', 
    basePrice: 6.12,
    sector: '银行',
    market: 'SH'
  },  '600000': { 
    name: '浦发银行', 
    basePrice: 8.45,
    sector: '银行',
    market: 'SH'
  },
  '000858': { 
    name: '五粮液', 
    basePrice: 158.60,
    sector: '白酒',
    market: 'SZ'
  },
  '002415': { 
    name: '海康威视', 
    basePrice: 28.90,
    sector: '电子',
    market: 'SZ'
  },
  '600276': { 
    name: '恒瑞医药', 
    basePrice: 42.80,
    sector: '医药',
    market: 'SH'
  },
  '300059': { 
    name: '东方财富', 
    basePrice: 15.80,
    sector: '金融',
    market: 'SZ'
  },
  '002142': { 
    name: '宁波银行', 
    basePrice: 32.40,
    sector: '银行',
    market: 'SZ'
  },
  '600690': { 
    name: '海尔智家', 
    basePrice: 23.50,
    sector: '家电',
    market: 'SH'
  },
  '601012': { 
    name: '隆基绿能', 
    basePrice: 18.90,
    sector: '新能源',
    market: 'SH'
  },
  '300760': { 
    name: '迈瑞医疗', 
    basePrice: 268.50,
    sector: '医疗器械',
    market: 'SZ'
  },
  '002594': { 
    name: 'BYD', 
    basePrice: 214.80,
    sector: '新能源汽车',
    market: 'SZ'
  },
  '300124': { 
    name: '汇川技术', 
    basePrice: 58.20,
    sector: '工业自动化',
    market: 'SZ'
  },
  '600309': { 
    name: '万华化学', 
    basePrice: 78.60,
    sector: '化工',
    market: 'SH'
  },
  '000568': { 
    name: '泸州老窖', 
    basePrice: 98.70,
    sector: '白酒',
    market: 'SZ'
  },
  '002304': { 
    name: '洋河股份', 
    basePrice: 89.40,
    sector: '白酒',
    market: 'SZ'
  }
}

/**
 * 生成基于时间的稳定价格数据
 * 使用日期作为种子，确保同一天内价格相对稳定
 */
function generateStablePrice(stockCode, basePrice) {
  const today = new Date().toDateString()
  const seed = hashString(today + stockCode)
  
  // 使用种子生成-3%到+3%的变化
  const changePercent = (seed % 600 - 300) / 100 // -3.00 到 +3.00
  const currentPrice = basePrice * (1 + changePercent / 100)
  
  return {
    currentPrice: currentPrice.toFixed(2),
    change: changePercent.toFixed(2),
    changeAmount: (currentPrice - basePrice).toFixed(2)
  }
}

/**
 * 简单的字符串哈希函数
 */
function hashString(str) {
  let hash = 0
  for (let i = 0; i < str.length; i++) {
    const char = str.charCodeAt(i)
    hash = ((hash << 5) - hash) + char
    hash = hash & hash // 转换为32位整数
  }
  return Math.abs(hash)
}

/**
 * 获取股票的实时数据（模拟）
 */
export function getStockRealtimeData(stockCode) {
  const baseData = STOCK_BASE_DATA[stockCode]
  if (!baseData) {
    return {
      code: stockCode,
      name: `股票${stockCode}`,
      currentPrice: '0.00',
      change: '0.00',
      changeAmount: '0.00',
      market: 'Unknown'
    }
  }
  
  const priceData = generateStablePrice(stockCode, baseData.basePrice)
  
  return {
    code: stockCode,
    name: baseData.name,
    sector: baseData.sector,
    market: baseData.market,
    ...priceData,
    openPrice: (parseFloat(priceData.currentPrice) * (1 + Math.random() * 0.02 - 0.01)).toFixed(2),
    highPrice: (parseFloat(priceData.currentPrice) * (1 + Math.random() * 0.03)).toFixed(2),
    lowPrice: (parseFloat(priceData.currentPrice) * (1 - Math.random() * 0.03)).toFixed(2),
    volume: generateVolume(stockCode),
    turnover: generateTurnover(stockCode)
  }
}

/**
 * 生成成交量（基于股票代码的稳定值）
 */
function generateVolume(stockCode) {
  const seed = hashString(stockCode)
  const volume = (seed % 10000 + 1000) * 100 // 10万到100万手
  return (volume / 10000).toFixed(1) + '万手'
}

/**
 * 生成成交额（基于股票代码的稳定值）
 */
function generateTurnover(stockCode) {
  const seed = hashString(stockCode + 'turnover')
  const turnover = (seed % 50 + 10) / 10 // 1.0到6.0亿
  return turnover.toFixed(1) + '亿'
}

/**
 * 批量获取多只股票的实时数据
 */
export function getBatchStockData(stockCodes) {
  return stockCodes.map(code => getStockRealtimeData(code))
}

/**
 * 更新自选股的价格信息
 */
export function updateWatchlistPrices(watchlist) {
  return watchlist.map(stock => {
    const realtimeData = getStockRealtimeData(stock.code)
    return {
      ...stock,
      ...realtimeData
    }
  })
}

/**
 * 检查是否需要发送价格提醒
 */
export function checkPriceAlerts(stockCode, currentPrice, alerts) {
  return alerts.filter(alert => {
    if (alert.stockCode !== stockCode || !alert.enabled) {
      return false
    }
    
    const price = parseFloat(currentPrice)
    const targetPrice = parseFloat(alert.targetPrice)
    
    switch (alert.condition) {
      case 'above':
        return price >= targetPrice
      case 'below':
        return price <= targetPrice
      case 'change_up':
        // 这里需要获取昨日收盘价进行比较
        return false // 暂时不实现
      case 'change_down':
        return false // 暂时不实现
      default:
        return false
    }
  })
}

/**
 * 生成股票排行榜数据
 */
export function generateStockRankingData() {
  const allStocks = Object.keys(STOCK_BASE_DATA).map(code => {
    const stockInfo = STOCK_BASE_DATA[code]
    const priceData = generateStablePrice(code, stockInfo.basePrice)
    return {
      code,
      name: stockInfo.name,
      sector: stockInfo.sector,
      market: stockInfo.market,
      basePrice: stockInfo.basePrice,
      currentPrice: parseFloat(priceData.currentPrice),
      change: parseFloat(priceData.change),
      changeAmount: parseFloat(priceData.changeAmount),
      volume: generateTradingVolume(code, stockInfo.basePrice)
    }
  })

  // 按涨跌幅排序获取涨幅榜
  const topGainers = allStocks
    .filter(stock => stock.change > 0)
    .sort((a, b) => b.change - a.change)
    .slice(0, 5)
    .map(stock => ({
      name: stock.name,
      code: stock.code,
      currentPrice: stock.currentPrice.toFixed(2),
      change: stock.change.toFixed(2)
    }))

  // 按涨跌幅排序获取跌幅榜  
  const topLosers = allStocks
    .filter(stock => stock.change < 0)
    .sort((a, b) => a.change - b.change)
    .slice(0, 5)
    .map(stock => ({
      name: stock.name,
      code: stock.code,
      currentPrice: stock.currentPrice.toFixed(2),
      change: stock.change.toFixed(2)
    }))

  // 按成交量排序获取成交额榜
  const topVolume = allStocks
    .sort((a, b) => parseFloat(b.volume.replace(/[亿万]/g, '')) - parseFloat(a.volume.replace(/[亿万]/g, '')))
    .slice(0, 5)
    .map(stock => ({
      name: stock.name,
      code: stock.code,
      currentPrice: stock.currentPrice.toFixed(2),
      volume: stock.volume
    }))

  return {
    topGainers,
    topLosers,
    topVolume
  }
}

/**
 * 生成交易量数据
 */
function generateTradingVolume(stockCode, basePrice) {
  const today = new Date().toDateString()
  const seed = hashString(today + stockCode + 'volume')
  
  // 根据股价和随机因子生成成交量
  let volumeBase
  if (basePrice > 1000) {
    // 高价股(如茅台)成交量较小
    volumeBase = (seed % 100 + 20) / 10 // 2-12亿
  } else if (basePrice > 100) {
    // 中高价股成交量中等
    volumeBase = (seed % 200 + 50) / 10 // 5-25亿
  } else if (basePrice > 10) {
    // 中价股成交量较大
    volumeBase = (seed % 300 + 100) / 10 // 10-40亿
  } else {
    // 低价股成交量最大
    volumeBase = (seed % 500 + 200) / 10 // 20-70亿
  }
  
  if (volumeBase >= 10) {
    return volumeBase.toFixed(1) + '亿'
  } else {
    return (volumeBase * 10).toFixed(0) + '万'
  }
}

/**
 * 生成模拟的股票历史数据
 * @param {string} stockCode - 股票代码
 * @param {string} timeRange - 时间范围: 'day', 'week', 'month', 'year'
 * @returns {Promise<Array<{time: string, price: number}>>} - 返回包含时间和价格的对象数组的Promise
 */
export function getStockHistoricalData(stockCode, timeRange) {
  const stockInfo = STOCK_BASE_DATA[stockCode];
  // 如果找不到股票信息，使用一个默认基础价格
  let basePrice = stockInfo ? stockInfo.basePrice : 50 + (hashString(stockCode) % 50); // 给未知股票一个基于代码的随机基础价
  const currentStablePriceData = stockInfo ? generateStablePrice(stockCode, basePrice) : null;
  const currentPrice = currentStablePriceData ? parseFloat(currentStablePriceData.currentPrice) : basePrice;

  const dataPoints = [];
  const now = new Date();

  switch (timeRange) {
    case 'day': {// 模拟当日的分时数据 (例如过去4小时，每5分钟一个点)
      // 设定一个模拟的开盘时间，比如9:30
      const marketOpenHour = 9;
      const marketOpenMinute = 30;
      let startTimeForDay = new Date(now);
      startTimeForDay.setHours(marketOpenHour, marketOpenMinute, 0, 0);

      // 如果当前时间早于开盘时间，则显示空数据或提示
      if (now < startTimeForDay && now.toDateString() === startTimeForDay.toDateString()) {
          // 可以返回空数组或特定格式表示未开盘
          return [];
      }
      
      // 确保我们从当天的开盘时间开始，或者如果已经过了很久，就从N小时前开始
      const fourHoursAgo = new Date(now.getTime() - 4 * 60 * 60 * 1000);
      startTimeForDay = fourHoursAgo > startTimeForDay ? fourHoursAgo : startTimeForDay;


      // 模拟从 startTimeForDay 到当前时间的点
      for (let t = new Date(startTimeForDay); t <= now; t.setMinutes(t.getMinutes() + 5)) {
        const pointTime = new Date(t);
        // 使用更稳定的基础价格进行波动，并逐渐趋向当前价格
        const timeDiffRatio = (pointTime.getTime() - startTimeForDay.getTime()) / (now.getTime() - startTimeForDay.getTime() || 1);
        const dynamicBase = basePrice + (currentPrice - basePrice) * timeDiffRatio * 0.8; // 价格逐渐向当前价靠拢

        const pseudoRandomFactor = (hashString(stockCode + pointTime.toISOString() + "price_day") % 1000 - 500) / 15000; // 日内波动幅度减小: -3.3% to +3.3%
        let price = dynamicBase * (1 + pseudoRandomFactor);
        price = Math.max(0.01, price); // 确保价格为正

        dataPoints.push({
          time: `${String(pointTime.getHours()).padStart(2, '0')}:${String(pointTime.getMinutes()).padStart(2, '0')}`,
          price: parseFloat(price.toFixed(2))
        });
      }
      // 确保最后一个点是当前价
      if (dataPoints.length > 0) {
        dataPoints[dataPoints.length-1].price = parseFloat(currentPrice.toFixed(2));
      } else if (currentStablePriceData) { // 如果循环未产生数据（例如时间间隔太短）
        dataPoints.push({
          time: `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`,
          price: parseFloat(currentPrice.toFixed(2))
        });
      }
      break;
    }

    case 'week': {// 模拟过去7天的每日收盘价
      for (let i = 6; i >= 0; i--) {
        const day = new Date(now);
        day.setDate(now.getDate() - i);
        // 价格围绕一个轻微的周趋势波动
        const trendFactor = ((3 - i) / 3) * 0.03; // 一周内约 +/-3% 的趋势
        const pseudoRandomFactor = (hashString(stockCode + day.toDateString() + "price_week") % 800 - 400) / 10000; // 波动: -4% to +4%
        let price = (i === 0 ? currentPrice : basePrice) * (1 + trendFactor + pseudoRandomFactor);
        price = Math.max(0.01, price);

        dataPoints.push({
          time: `${String(day.getMonth() + 1).padStart(2, '0')}-${String(day.getDate()).padStart(2, '0')}`,
          price: parseFloat(price.toFixed(2))
        });
      }
      break;
    }

    case 'month': {// 模拟过去30天的每日收盘价
      for (let i = 29; i >= 0; i--) {
        const day = new Date(now);
        day.setDate(now.getDate() - i);
        const trendFactor = ((15 - i) / 15) * 0.1; // 一个月内约 +/-10% 的趋势
        const pseudoRandomFactor = (hashString(stockCode + day.toDateString() + "price_month") % 1000 - 500) / 10000; // 波动: -5% to +5%
        let price = (i === 0 ? currentPrice : basePrice) * (1 + trendFactor + pseudoRandomFactor);
        price = Math.max(0.01, price);

        dataPoints.push({
          time: `${String(day.getMonth() + 1).padStart(2, '0')}-${String(day.getDate()).padStart(2, '0')}`,
          price: parseFloat(price.toFixed(2))
        });
      }
      break;
    }

    case 'year': {// 模拟过去52周的每周收盘价
      for (let i = 51; i >= 0; i--) {
        const day = new Date(now);
        day.setDate(now.getDate() - i * 7); // 每周一个点
        const trendFactor = ((26 - i) / 26) * 0.3; // 一年内约 +/-30% 的趋势
        const pseudoRandomFactor = (hashString(stockCode + day.getFullYear() + "week" + (Math.floor((day.getMonth() * 4 + day.getDate()/7))) + "price_year") % 2000 - 1000) / 10000; // 波动 -10% to +10%
        let price = (i === 0 ? currentPrice : basePrice) * (1 + trendFactor + pseudoRandomFactor);
        price = Math.max(0.01, price);

        dataPoints.push({
          // 年线用 年-月 格式可能更合适，或者 年-月-日
          time: `${day.getFullYear()}-${String(day.getMonth() + 1).padStart(2, '0')}`,
          price: parseFloat(price.toFixed(2))
        });
      }
      break;
    }
    default:
      return []; // 未知类型则返回空数组
  }
  return dataPoints;
}