<!-- eslint-disable vue/multi-word-component-names -->
<template>
    <div class="login-page">
      <div class="login-container">
        <!-- 左边图片 -->
        <div class="login-image">
          <img src="@/assets/login-side.jpg" alt="Login Illustration" />
        </div>
  
        <!-- 右边表单 -->
        <div class="login-form">
          <h2>密码登录</h2>
          <el-form :model="form" class="form">
            <el-form-item>
              <el-input v-model="form.username" placeholder="请输入用户名" size="large" />
            </el-form-item>
            <el-form-item>
              <el-input 
              v-model="form.password" 
              :type="showword ? 'text':'password'" 
              size="large"
              placeholder="请输入密码" 
              >
              <template #suffix>
                <el-icon @click = "showword = !showword" class="cursor-pointer" >
                  <component :is="showword ? 'View' : 'Hide'" />
                </el-icon>
              </template>
            </el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleLogin" class="login-btn" block>
                登录
              </el-button>
            </el-form-item>
            <el-form-item>
            <div class="register-link">
              还没有账户? <el-link type="primary" @click="goToRegister">立即注册</el-link>
            </div>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>
  </template>
  
  
  <script>
  import { ref } from 'vue'
  import { useRouter, useRoute } from 'vue-router'
  import { View, Hide } from '@element-plus/icons-vue'
  import { ElMessage } from 'element-plus'
  
  // 导入用户管理器
  import { validateUser, loginUser } from '@/utils/userManager'
  
  export default {
    name: 'LoginView',
    components: {
      View,
      Hide
    },
    setup() {
      const router = useRouter()
      const route = useRoute()
  
      const form = ref({
        username: '',
        password: ''
      })
      const showword = ref(false);
  
      function handleLogin() {
        if (!form.value.username || !form.value.password) {
          ElMessage.error('请输入用户名和密码')
          return
        }

        // 使用用户管理器验证用户
        const result = validateUser(form.value.username, form.value.password)
        
        if (result.success) {
          // 使用用户管理器处理登录
          loginUser(form.value.username)
          
          ElMessage.success('登录成功！')
          const redirect = route.query.redirect || '/dashboard'
          router.push(redirect)
        } else {
          ElMessage.error(result.message)
        }
      }
      
      function goToRegister() {
        router.push('/register') 
      }
      
      return {
        form,
        showword,
        handleLogin,
        goToRegister
      }
    }
  }
  </script>

  <style scoped>
  .login-page {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100vh;
    background-color: #f5f7fa;
  }
  
  .login-container {
    display: flex;
    width: 1000px;
    height: 500px;
    background-color: white;
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.2);
    overflow: hidden;
  }
  
  .login-image {
    flex: 1.5;
    background-color: #f0f0f0;
  }
  
  .login-image img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  
  .login-form {
    flex: 1;
    padding: 50px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center; 
  }
  
  .login-form h2 {
    margin-bottom: 50px;
    font-size: 30px;
    color: #333;
  }
  
  .form {
    width: 90%;
    justify-content: center;
  }
  
  .login-btn {
    width: 100%;
  }

  .cursor-pointer {
    cursor: pointer;
  }
  .register-link {
    width: 100%;
    text-align: center;
    margin-top: 10px; /* 与登录按钮隔开一些距离 */
    font-size: 14px;
  }
  .register-link .el-link {
    vertical-align: baseline; /* 调整el-link对齐 */
}
  </style>