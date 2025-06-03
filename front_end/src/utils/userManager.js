/**
 * 用户管理工具
 * 提供用户切换、数据初始化和清理功能
 */

import { initializeDefaultRecords } from './tradeManager'
import { initializeDefaultWatchlist } from './stockManager'
import { initializeUserFunds } from './fundManager'
import { initializeNewUserPositions } from './positionManager'
import { syncAuthState } from '../store/authStore'

/**
 * 获取当前登录用户名
 */
export function getCurrentUser() {
  return sessionStorage.getItem('loggedInUserDemo') || null
}

/**
 * 检查用户是否已登录
 */
export function isUserLoggedIn() {
  return sessionStorage.getItem('isLoggedIn') === 'true'
}

/**
 * 用户登录处理
 * @param {string} username - 用户名
 */
export function loginUser(username) {
  sessionStorage.setItem('isLoggedIn', 'true')
  sessionStorage.setItem('loggedInUserDemo', username)

  syncAuthState() // 同步认证状态到 store
  
  // 初始化用户数据（登录时不初始化持仓）
  initializeExistingUserData()
  
  console.log(`用户 ${username} 登录成功，数据初始化完成`)
}

/**
 * 用户登出处理
 */
export function logoutUser() {
  const currentUser = getCurrentUser()
  
  sessionStorage.removeItem('isLoggedIn')
  sessionStorage.removeItem('loggedInUserDemo')

  syncAuthState() // 同步认证状态到 store
  
  console.log(`用户 ${currentUser} 已登出`)
}

/**
 * 为新注册用户初始化完整数据
 * 包括所有股票的初始持仓
 */
export function initializeNewUserData() {
  const currentUser = getCurrentUser()
  if (!currentUser) {
    console.warn('没有当前用户，跳过数据初始化')
    return
  }
  
  try {
    console.log(`开始为新用户 ${currentUser} 初始化完整数据...`)
    
    // 初始化各类数据
    initializeUserFunds()              // 初始化资金
    initializeNewUserPositions()       // 初始化持仓（每只股票30股）
    initializeDefaultRecords()         // 初始化交易记录
    initializeDefaultWatchlist()       // 初始化自选股
    
    console.log(`新用户 ${currentUser} 数据初始化完成`)
  } catch (error) {
    console.error(`新用户 ${currentUser} 数据初始化失败:`, error)
  }
}

/**
 * 为已存在用户初始化数据（不包括持仓）
 * 登录时使用，不会重新初始化持仓
 */
export function initializeExistingUserData() {
  const currentUser = getCurrentUser()
  if (!currentUser) {
    console.warn('没有当前用户，跳过数据初始化')
    return
  }
  
  try {
    console.log(`开始为现有用户 ${currentUser} 检查和初始化数据...`)
    
    // 只初始化基础数据，不初始化持仓
    initializeUserFunds()          // 初始化资金
    initializeDefaultRecords()     // 初始化交易记录
    initializeDefaultWatchlist()   // 初始化自选股
    
    console.log(`现有用户 ${currentUser} 数据检查完成`)
  } catch (error) {
    console.error(`现有用户 ${currentUser} 数据初始化失败:`, error)
  }
}

/**
 * 初始化用户数据（兼容旧接口，现在调用现有用户数据初始化）
 * 为当前用户初始化所有必要的数据（如果数据不存在）
 */
export function initializeUserData() {
  initializeExistingUserData()
}

/**
 * 检查和初始化用户数据
 * 通常在应用启动或页面刷新时调用
 */
export function checkAndInitializeUser() {
  if (isUserLoggedIn()) {
    const currentUser = getCurrentUser()
    if (currentUser) {
      syncAuthState() // 同步认证状态到 store
      initializeExistingUserData()
      console.log(`检测到已登录用户 ${currentUser}，数据已初始化`)
    } else {
      // 如果标记为已登录但没有用户名，清理登录状态
      logoutUser()
      console.warn('检测到无效登录状态，已清理')
    }
  }
  else
  {
    syncAuthState() // 同步认证状态到 store
    console.log('当前没有用户登录，跳过数据初始化')
  }
}

/**
 * 获取所有已注册用户
 */
export function getAllUsers() {
  try {
    const users = localStorage.getItem('demoUsers')
    return users ? JSON.parse(users) : []
  } catch (error) {
    console.error('获取用户列表失败:', error)
    return []
  }
}

/**
 * 验证用户凭据
 * @param {string} username - 用户名
 * @param {string} password - 密码
 * @returns {object} 验证结果
 */
export function validateUser(username, password) {
  const users = getAllUsers()
  const user = users.find(u => u.username === username)
  
  if (!user) {
    return { success: false, message: '用户不存在，请先注册' }
  }
  
  if (user.password !== password) {
    return { success: false, message: '密码错误' }
  }
  
  return { success: true, user }
}

/**
 * 注册新用户
 * @param {object} userData - 用户数据
 */
export function registerUser(userData) {
  try {
    const users = getAllUsers()
    
    // 检查用户名是否已存在
    if (users.some(u => u.username === userData.username)) {
      return { success: false, message: '用户名已存在' }
    }
    
    // 添加新用户
    const newUser = {
      ...userData,
      createTime: new Date().getTime()
    }
    
    users.push(newUser)
    localStorage.setItem('demoUsers', JSON.stringify(users))
    
    // 自动登录新用户
    sessionStorage.setItem('isLoggedIn', 'true')
    sessionStorage.setItem('loggedInUserDemo', userData.username)

    syncAuthState() // 同步认证状态到 store
    
    // 为新用户初始化完整数据（包括持仓）
    initializeNewUserData()
    
    console.log(`新用户 ${userData.username} 注册成功并已初始化数据`)
    return { success: true, user: newUser }
  } catch (error) {
    console.error('用户注册失败:', error)
    return { success: false, message: '注册失败，请重试' }
  }
} 