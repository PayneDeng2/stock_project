import { reactive, readonly } from 'vue';

// 内部响应式状态，初始值从 sessionStorage 读取
const state = reactive({
  isLoggedIn: sessionStorage.getItem('isLoggedIn') === 'true',
  username: sessionStorage.getItem('loggedInUserDemo') || null,
});

/**
 * 同步共享认证状态
 * 这个函数应该在任何直接修改了 sessionStorage 中认证信息的地方被调用，
 * 或者在应用初始化时调用，以确保共享状态与 sessionStorage 一致。
 */
export function syncAuthState() {
  state.isLoggedIn = sessionStorage.getItem('isLoggedIn') === 'true';
  state.username = sessionStorage.getItem('loggedInUserDemo') || null;
  console.log('[authStore] Auth state synced:', { isLoggedIn: state.isLoggedIn, username: state.username });
}

// 导出只读版本的状态，供组件订阅和使用，防止外部直接修改
export const authState = readonly(state);