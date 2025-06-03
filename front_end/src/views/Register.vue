<template>
    <div class="register-page">
      <div class="register-container">
        <div class="register-image">
          <img src="@/assets/login-side.jpg" alt="Register Illustration" />
          </div>
  
        <div class="register-form-section">
          <h2>创建您的账户</h2>
          <el-form :model="form" :rules="rules" ref="registerFormRef" class="form">
            <el-form-item prop="username">
              <el-input v-model="form.username" placeholder="请输入用户名" size="large" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="form.password"
                :type="showPassword ? 'text' : 'password'"
                size="large"
                placeholder="请输入密码 (至少6位)"
              >
                <template #suffix>
                  <el-icon @click="showPassword = !showPassword" class="cursor-pointer">
                    <component :is="showPassword ? 'View' : 'Hide'" />
                  </el-icon>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item prop="confirmPassword">
              <el-input
                v-model="form.confirmPassword"
                :type="showConfirmPassword ? 'text' : 'password'"
                size="large"
                placeholder="请再次输入密码"
              >
                <template #suffix>
                  <el-icon @click="showConfirmPassword = !showConfirmPassword" class="cursor-pointer">
                    <component :is="showConfirmPassword ? 'View' : 'Hide'" />
                  </el-icon>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleRegister" class="action-btn" block>
                注册
              </el-button>
            </el-form-item>
            <el-form-item>
              <div class="navigation-link">
                已有账户? <el-link type="primary" @click="goToLogin">立即登录</el-link>
              </div>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>
  </template>
  
  <script>
  import { ref } from 'vue'
  import { useRouter } from 'vue-router'
  import { View, Hide } from '@element-plus/icons-vue' 
  import { ElMessage } from 'element-plus'
  
  // 导入用户管理器
  import { registerUser } from '@/utils/userManager'
  
  export default {
    name: 'RegisterView', 
    components: {
      View, 
      Hide  
    },
    setup() {
      const router = useRouter()
      const registerFormRef = ref(null)
  
      const form = ref({
        username: '',
        password: '',
        confirmPassword: ''
      })
  
      const showPassword = ref(false)
      const showConfirmPassword = ref(false)
  
      const validatePassConfirm = (rule, value, callback) => {
        if (value === '') {
          callback(new Error('请再次输入密码'))
        } else if (value !== form.value.password) {
          callback(new Error("两次输入的密码不一致!"))
        } else {
          callback()
        }
      }
  
      const rules = ref({
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 3, max: 15, message: '长度在 3 到 15 个字符', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, validator: validatePassConfirm, trigger: 'blur' }
        ]
      })
  
      const handleRegister = async () => {
        if (!registerFormRef.value) return
        await registerFormRef.value.validate((valid) => {
          if (valid) {
            // 使用用户管理器注册用户
            const result = registerUser({
              username: form.value.username,
              password: form.value.password
            })

            if (result.success) {
              ElMessage.success('注册成功！已自动登录，即将跳转到首页...')
              setTimeout(() => {
                router.push('/dashboard')
              }, 1500)
            } else {
              ElMessage.error(result.message)
            }
          } else {
            ElMessage.error('请检查信息是否正确填写')
            return false
          }
        })
      }
  
      const goToLogin = () => {
        router.push('/login')
      }
  
      return {
        form,
        rules,
        registerFormRef,
        showPassword,
        showConfirmPassword,
        handleRegister,
        goToLogin
      }
    }
  }
  </script>
  
  <style scoped>
  .register-page {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100vh;
    background-color: #f5f7fa; 
  }
  
  .register-container {
    display: flex;
    width: 1000px; 
    height: 500px; 
    background-color: white;
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.2);
    overflow: hidden;
  }
  
  .register-image {
    flex: 1.5; 
    background-color: #f0f0f0;
  }
  
  .register-image img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  
  .register-form-section { 
    flex: 1; 
    padding: 50px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    overflow-y: auto; 
  }
  
  .register-form-section h2 {
    margin-bottom: 50px;
    font-size: 30px;
    color: #333;
  }
  
  .form {
    width: 90%; 
    justify-content: center;
  }
  
  .action-btn { 
    width: 100%;
  }
  
  .cursor-pointer {
    cursor: pointer;
  }
  
  .navigation-link { 
    width: 100%;
    text-align: center;
    margin-top: 10px;
    font-size: 14px;
  }
  .navigation-link .el-link {
    vertical-align: baseline;
  }
  </style>