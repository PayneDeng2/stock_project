<template>
  <div class="profile-container">
    <el-tabs v-model="activeTab" type="border-card" class="custom-tabs">
      <!-- 个人信息面板 -->
      <el-tab-pane label="个人信息" name="info">
        <div class="info-section">
          <el-descriptions
            title="个人信息"
            :column="1"
             border
            class="info-descriptions"
          >
            <el-descriptions-item label="用户名">
              <el-icon class="info-icon"><User /></el-icon>
              <span class="info-text">ABC</span>
            </el-descriptions-item>

            <el-descriptions-item label="邮箱">
              <el-icon class="info-icon"><Message /></el-icon>
              <span class="info-text">ABC@example.com</span>
            </el-descriptions-item>

            <el-descriptions-item label="手机号">
              <el-icon class="info-icon"><Phone /></el-icon>
              <span class="info-text">123-4567-8910</span>
            </el-descriptions-item>
          </el-descriptions>

          <div class="logout-wrap">
            <el-button type="danger" plain @click="logout" class="logout-button" size="large">
              登出
            </el-button>
          </div>
        </div>
      </el-tab-pane>

      <!-- 修改密码面板保持不变 -->
      <el-tab-pane label="修改密码" name="password">
        <div class="password-section">
          <el-form :model="form" :rules="passwordRules" ref="passwordFormRef" label-width="120px">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input
                v-model="form.oldPassword"
                :type="showOld ? 'text' : 'password'"
                size="large"
                autocomplete="off"
              >
                <template #suffix>
                  <el-icon @click="showOld = !showOld" class="cursor-pointer">
                    <component :is="showOld ? 'View' : 'Hide'" />
                  </el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="form.newPassword"
                :type="showNew ? 'text' : 'password'"
                size="large"
                autocomplete="off"
              >
                <template #suffix>
                  <el-icon @click="showNew = !showNew" class="cursor-pointer">
                    <component :is="showNew ? 'View' : 'Hide'" />
                  </el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="form.confirmPassword"
                :type="showConfirm ? 'text' : 'password'"
                size="large"
                autocomplete="off"
              >
                <template #suffix>
                  <el-icon @click="showConfirm = !showConfirm" class="cursor-pointer">
                    <component :is="showConfirm ? 'View' : 'Hide'" />
                  </el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleChangePassword" size="large">提交</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import { ref,onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { View, Hide, User, Message, Phone } from '@element-plus/icons-vue';
import {useRouter} from 'vue-router';

export default {
  name: 'ProfileView',
  components: {
    View,
    Hide,
    User,
    Message,
    Phone
  },
  setup() {
    const router = useRouter();
    const activeTab = ref('info');
    const displayedUsername = ref(''); // 初始化为空字符串，在onMounted中设置
    const passwordFormRef = ref(null);

    const form = ref({
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    });

    const showOld = ref(false);
    const showNew = ref(false);
    const showConfirm = ref(false);

    // const fixedPassword = '123456'; // 设定固定原密码
    onMounted(() => {
      // 只需要从sessionStorage获取用户名用于显示
      const usernameFromSession = sessionStorage.getItem('loggedInUserDemo');
      if (usernameFromSession) {
        displayedUsername.value = usernameFromSession;
      } else {
        displayedUsername.value = '用户（未获取到信息）';
        ElMessage.error('无法获取当前用户信息，请尝试重新登录。');
        router.push('/login');
      }
    });

    const validateNewPasswordConfirm = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请再次输入新密码'));
      } else if (value !== form.value.newPassword) {
        callback(new Error("两次输入的新密码不一致!"));
      } else {
        callback();
      }
    };

    const passwordRules = ref({
      oldPassword: [
        { required: true, message: '请输入原密码', trigger: 'blur' }
      ],
      newPassword: [
        { required: true, message: '请输入新密码', trigger: 'blur' },
        { min: 6, message: '新密码长度不能少于6位', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, validator: validateNewPasswordConfirm, trigger: 'blur' }
      ]
    });
    
    const handleChangePassword = async () => {
      if (!passwordFormRef.value) return;

      await passwordFormRef.value.validate(async (valid) => {
        if (valid) {
          const loggedInUsername = sessionStorage.getItem('loggedInUserDemo');
          // 虽然onMounted时获取了，但操作时再次确认是好习惯
          if (!loggedInUsername) {
            ElMessage.error('会话可能已过期，请重新登录后再试。');
            // 你可以根据需要决定是否在这里强制跳转，或者依赖外部路由守卫
            sessionStorage.removeItem('isLoggedIn');
            router.push('/login');
            return;
          }

          let users = JSON.parse(localStorage.getItem('demoUsers') || '[]');
          const userIndex = users.findIndex(user => user.username === loggedInUsername);

          if (userIndex === -1) {
            ElMessage.error('当前用户信息在本地存储中未找到！');
            return;
          }

          const currentUser = users[userIndex];

          if (form.value.oldPassword !== currentUser.password) {
            ElMessage.error('原密码错误！');
            return;
          }

          users[userIndex].password = form.value.newPassword;
          localStorage.setItem('demoUsers', JSON.stringify(users));

          ElMessage.success('密码修改成功！');
          // form.value.oldPassword = '';
          // form.value.newPassword = '';
          // form.value.confirmPassword = '';
          if(passwordFormRef.value) {
            passwordFormRef.value.resetFields();
          }
          // form.value.resetFields();
        } else {
          ElMessage.error('请检查表单输入项。');
          return false;
        }
      });
    };
    // const changePassword = () => {
    //   if (form.value.oldPassword !== fixedPassword) {
    //     ElMessage.error('原密码错误！');
    //     return;
    //   }
    //   if (form.value.newPassword !== form.value.confirmPassword) {
    //     ElMessage.error('新密码和确认密码不一致！');
    //     return;
    //   }

    //   console.log('修改密码提交：', form.value);
    //   ElMessage.success('密码修改成功！');
    // };

    // const logout = () => {
    //   console.log('用户登出');
    // };
    // const router = useRouter();
    function logout() {
    sessionStorage.removeItem('isLoggedIn')
    sessionStorage.removeItem('loggedInUserDemo'); // 清除用户名
    ElMessage.success('登出成功！');
    router.push('/Dashboard')    
    }

    return {
      activeTab,
      form,
      passwordFormRef,
      passwordRules,
      showOld,
      showNew,
      showConfirm,
      handleChangePassword,
      displayedUsername,
      logout
    };
  }
};
</script>

<style scoped>
.profile-container {
  padding: 40px;
  display: flex;
  justify-content: center;
  font-size: 18px;
  background-color: #f5f7fa;
}

.custom-tabs {
  width: 700px;
  font-size: 18px;
  background-color: white;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  border-radius: 8px;
}

.el-tabs__item {
  font-size: 18px !important;
  padding: 16px 30px !important;
}

.info-section {
  padding: 30px 20px;
}

.info-descriptions {
  font-size: 16px;
  margin-bottom: 40px;
}

.info-icon {
  margin-right: 8px;
  vertical-align: middle;
  color: #409EFF;
}

.info-text {
  vertical-align: middle;
}

.logout-wrap {
  text-align: center;
}

.logout-button {
  font-size: 16px;
  padding: 10px 30px;
}

.password-section {
  max-width: 450px;
  margin: 0 auto;
  padding-top: 40px;
  font-size: 16px;
}

.cursor-pointer {
  cursor: pointer;
}
</style>
