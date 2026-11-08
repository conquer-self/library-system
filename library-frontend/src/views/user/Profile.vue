<template>
  <div class="profile-page">
    <h1 class="page-title">个人中心</h1>

    <div class="profile-container">
      <!-- 个人信息卡片 -->
      <div class="info-card card-base">
        <div class="card-header">
          <h2>个人信息</h2>
          <el-button type="primary" @click="showPasswordDialog = true">
            <el-icon><Lock /></el-icon>
            修改密码
          </el-button>
        </div>

        <el-form
          :model="formData"
          :rules="rules"
          ref="formRef"
          label-width="100px"
          class="profile-form"
        >
          <el-form-item label="学号/账号">
            <el-input v-model="formData.username" disabled />
          </el-form-item>

          <el-form-item label="真实姓名" prop="realName">
            <el-input
              v-model="formData.realName"
              placeholder="请输入真实姓名"
            />
          </el-form-item>

          <el-form-item label="手机号" prop="phone">
            <el-input v-model="formData.phone" placeholder="请输入手机号" />
          </el-form-item>

          <el-form-item label="邮箱" prop="email">
            <el-input v-model="formData.email" placeholder="请输入邮箱" />
          </el-form-item>

          <el-form-item label="账号角色">
            <el-tag :type="formData.role === 'admin' ? 'danger' : 'primary'">
              {{ formData.role === "admin" ? "管理员" : "普通用户" }}
            </el-tag>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleSave" :loading="loading">
              保存修改
            </el-button>
            <el-button @click="fetchUserInfo">取消</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 账号统计 -->
      <div class="stats-card card-base">
        <h3>账号统计</h3>
        <div class="stat-item">
          <span class="stat-label">注册时间</span>
          <span class="stat-value">{{ formatDate(userInfo.createTime) }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">账号状态</span>
          <el-tag
            :type="userInfo.status === 1 ? 'success' : 'danger'"
            size="small"
          >
            {{ userInfo.status === 1 ? "正常" : "禁用" }}
          </el-tag>
        </div>
      </div>
    </div>

    <!-- 修改密码对话框 -->
    <el-dialog
      v-model="showPasswordDialog"
      title="修改密码"
      width="450px"
      @close="resetPasswordForm"
    >
      <el-form
        :model="passwordForm"
        :rules="passwordRules"
        ref="passwordFormRef"
        label-width="100px"
      >
        <el-form-item label="原密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            placeholder="请输入原密码"
            show-password
          />
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码（6位以上）"
            show-password
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showPasswordDialog = false">取消</el-button>
        <el-button
          type="primary"
          @click="handlePasswordChange"
          :loading="passwordLoading"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { useUserStore } from "@/stores/user";
import { getUserInfo, updateUserInfo, updatePassword } from "@/api/user";
import { Lock } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";

const userStore = useUserStore();
const formRef = ref();
const passwordFormRef = ref();
const loading = ref(false);
const passwordLoading = ref(false);
const showPasswordDialog = ref(false);
const userInfo = ref({});

const formData = reactive({
  username: "",
  realName: "",
  phone: "",
  email: "",
  role: "user",
});

const passwordForm = reactive({
  oldPassword: "",
  newPassword: "",
  confirmPassword: "",
});

// 表单验证规则
const rules = {
  realName: [
    { min: 2, max: 10, message: "长度在2到10个字符", trigger: "blur" },
  ],
  phone: [
    {
      pattern: /^1[3-9]\d{9}$/,
      message: "请输入正确的手机号",
      trigger: "blur",
    },
  ],
  email: [
    {
      type: "email",
      message: "请输入正确的邮箱地址",
      trigger: "blur",
    },
  ],
};

// 密码表单验证
const validateNewPassword = (rule, value, callback) => {
  if (value === "") {
    callback(new Error("请输入新密码"));
  } else if (value.length < 6) {
    callback(new Error("密码长度不能少于6位"));
  } else {
    if (passwordForm.confirmPassword !== "") {
      passwordFormRef.value.validateField("confirmPassword");
    }
    callback();
  }
};

const validateConfirmPassword = (rule, value, callback) => {
  if (value === "") {
    callback(new Error("请再次输入密码"));
  } else if (value !== passwordForm.newPassword) {
    callback(new Error("两次输入密码不一致"));
  } else {
    callback();
  }
};

const passwordRules = {
  oldPassword: [{ required: true, message: "请输入原密码", trigger: "blur" }],
  newPassword: [
    { required: true, validator: validateNewPassword, trigger: "blur" },
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: "blur" },
  ],
};

// 获取用户信息
const fetchUserInfo = async () => {
  try {
    const res = await getUserInfo();
    userInfo.value = res.data;
    Object.assign(formData, res.data);
  } catch (error) {
    console.error(error);
  }
};

// 保存修改
const handleSave = async () => {
  try {
    await formRef.value.validate();
    loading.value = true;

    await updateUserInfo({
      realName: formData.realName,
      phone: formData.phone,
      email: formData.email,
    });

    ElMessage.success("保存成功");
    fetchUserInfo();

    // 更新store中的用户信息
    const updatedUser = { ...userStore.userInfo, ...formData };
    userStore.setUser(updatedUser, userStore.token);
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};

// 修改密码
const handlePasswordChange = async () => {
  try {
    await passwordFormRef.value.validate();
    passwordLoading.value = true;

    await updatePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
    });

    ElMessage.success("密码修改成功");
    showPasswordDialog.value = false;
    resetPasswordForm();
  } catch (error) {
    console.error(error);
  } finally {
    passwordLoading.value = false;
  }
};

// 重置密码表单
const resetPasswordForm = () => {
  passwordForm.oldPassword = "";
  passwordForm.newPassword = "";
  passwordForm.confirmPassword = "";
  passwordFormRef.value?.clearValidate();
};

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return "-";
  return new Date(dateStr).toLocaleDateString("zh-CN");
};

onMounted(() => {
  fetchUserInfo();
});
</script>

<style scoped>
.profile-page {
  max-width: 900px;
  margin: 0 auto;
  padding-bottom: 40px;
}

.profile-container {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
}

.info-card,
.stats-card {
  padding: 24px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-light);
}

.card-header h2 {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.profile-form :deep(.el-input.is-disabled .el-input__wrapper) {
  background-color: #f5f7fa;
  cursor: not-allowed;
}

.stats-card h3 {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 20px 0;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-light);
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid var(--border-light);
}

.stat-item:last-child {
  border-bottom: none;
}

.stat-label {
  font-size: 14px;
  color: var(--text-secondary);
}

.stat-value {
  font-size: 14px;
  color: var(--text-primary);
  font-weight: 500;
}

/* 响应式 */
@media (max-width: 768px) {
  .profile-container {
    grid-template-columns: 1fr;
  }
}
</style>
