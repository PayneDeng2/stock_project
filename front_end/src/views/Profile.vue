<template>
  <div class="profile-container">
    <el-tabs v-model="activeTab" type="border-card" class="custom-tabs">
      <el-tab-pane label="个人信息" name="info">
        <div class="info-section">
          <el-form :model="profileInfoForm" :rules="profileInfoRules" ref="profileInfoFormRef" label-width="100px" class="profile-info-form">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="profileInfoForm.username" disabled size="large">
                <template #prepend><el-icon><User /></el-icon></template>
              </el-input>
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="profileInfoForm.email" placeholder="请输入邮箱" size="large">
                <template #prepend><el-icon><Message /></el-icon></template>
              </el-input>
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="profileInfoForm.phone" placeholder="请输入手机号" size="large">
                <template #prepend><el-icon><Phone /></el-icon></template>
              </el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSaveProfileInfo">保存个人信息</el-button>
            </el-form-item>
          </el-form>

          <div class="logout-wrap">
            <el-button type="danger" plain @click="logout" class="logout-button" size="large">
              登出
            </el-button>
          </div>
        </div>
      </el-tab-pane>

      <!-- 修改密码-->
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
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { View, Hide, User, Message, Phone } from '@element-plus/icons-vue'; 
import { useRouter } from 'vue-router';

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

    // 个人信息表单相关
    const profileInfoFormRef = ref(null);
    const profileInfoForm = ref({
      username: '', 
      email: '',
      phone: ''
    });
    const profileInfoRules = ref({
      email: [
        { type: 'email', message: '请输入正确的邮箱格式', trigger: ['blur', 'change'] }
      ],
      phone: [
        { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式 (如果填写)', trigger: ['blur', 'change'] }
      ]
    });


    const passwordFormRef = ref(null);
    const passwordForm = ref({ 
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    });
    const showOld = ref(false); 
    const showNew = ref(false); 
    const showConfirm = ref(false); 

    onMounted(() => {
      const usernameFromSession = sessionStorage.getItem('loggedInUserDemo'); 
      const isLoggedIn = sessionStorage.getItem('isLoggedIn');

      if (!isLoggedIn || isLoggedIn !== 'true' || !usernameFromSession) {
        ElMessage.error('无法获取当前用户信息或会话已过期，请重新登录。');
        router.push('/login');
        return;
      }


      profileInfoForm.value.username = usernameFromSession;


      const users = JSON.parse(localStorage.getItem('demoUsers') || '[]');
      const currentUserData = users.find(user => user.username === usernameFromSession);

      if (currentUserData) {
        profileInfoForm.value.email = currentUserData.email || ''; 
        profileInfoForm.value.phone = currentUserData.phone || ''; 
      } else {
        ElMessage.error('在本地存储中未找到当前用户的详细信息。');
        // 可以选择登出
        logout();
      }
    });

    const handleSaveProfileInfo = async () => {
      if (!profileInfoFormRef.value) {
        ElMessage.error('个人信息表单引用错误。');
        return;
      }
      await profileInfoFormRef.value.validate(async (valid) => {
        if (valid) {
          const loggedInUsername = profileInfoForm.value.username; 
          let users = JSON.parse(localStorage.getItem('demoUsers') || '[]');
          const userIndex = users.findIndex(user => user.username === loggedInUsername);

          if (userIndex !== -1) {
            users[userIndex].email = profileInfoForm.value.email;
            users[userIndex].phone = profileInfoForm.value.phone;
            localStorage.setItem('demoUsers', JSON.stringify(users));
            ElMessage.success('个人信息保存成功！');
          } else {
            ElMessage.error('保存失败：当前用户信息在本地存储中未找到！');
          }
        } else {
          ElMessage.error('请检查个人信息表单输入项。');
        }
      });
    };

    const validateNewPasswordConfirm = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请再次输入新密码'));
      } else if (value !== passwordForm.value.newPassword) { 
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
      if (!passwordFormRef.value) { 
         ElMessage.error('密码表单引用错误。');
         return;
      }

      await passwordFormRef.value.validate(async (valid) => {
        if (valid) {
          const loggedInUsername = profileInfoForm.value.username; 

          if (!loggedInUsername) {
            ElMessage.error('会话可能已过期或用户身份无法确认，请重新登录后再试。');
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

          if (passwordForm.value.oldPassword !== currentUser.password) { 
            ElMessage.error('原密码错误！');
            return;
          }

          users[userIndex].password = passwordForm.value.newPassword; 
          localStorage.setItem('demoUsers', JSON.stringify(users));

          ElMessage.success('密码修改成功！');
          if (passwordFormRef.value) {
            passwordFormRef.value.resetFields();
          }
        } else {
          ElMessage.error('请检查密码表单的输入项。');
          return false;
        }
      });
    };
    
    const logout = () => {
      sessionStorage.removeItem('isLoggedIn');
      sessionStorage.removeItem('loggedInUserDemo'); //
      ElMessage.success('登出成功！');
      router.push('/Dashboard');
    };

    return {
      activeTab,
      // 个人信息表单
      profileInfoForm,
      profileInfoFormRef,
      profileInfoRules,
      handleSaveProfileInfo, 
      // 修改密码表单
      form: passwordForm, 
      passwordFormRef,   
      passwordRules,      
      showOld,           
      showNew,           
      showConfirm,        
      handleChangePassword,
      logout,
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
