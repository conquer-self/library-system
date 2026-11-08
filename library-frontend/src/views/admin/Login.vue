<template>
  <div class="admin-login-page">
    <!-- 左侧装饰区 -->
    <div class="login-left">
      <div class="login-decoration">
        <h1 class="slogan">管理员入口</h1>
        <h2 class="sub-slogan">图书管理系统</h2>
        <div class="decoration-icon">🔐</div>
      </div>
    </div>

    <!-- 右侧表单区 -->
    <div class="login-right">
      <div class="login-form-container">
        <h1 class="login-title">管理员登录</h1>
        <p class="login-subtitle">欢迎回来，请使用管理员账号登录</p>

        <el-form
          :model="formData"
          :rules="rules"
          ref="formRef"
          class="login-form"
        >
          <el-form-item prop="username">
            <el-input
              v-model="formData.username"
              placeholder="请输入管理员账号"
              size="large"
              clearable
            >
              <template #prefix>
                <el-icon><UserFilled /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="formData.password"
              type="password"
              placeholder="请输入管理员密码"
              size="large"
              show-password
              @keyup.enter="handleLogin"
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-button
            type="primary"
            size="large"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form>

        <div class="login-footer">
          <el-link type="info" @click="router.push('/login')">
            <el-icon><ArrowLeft /></el-icon>
            返回学生登录
          </el-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";
import { useUserStore } from "@/stores/user";
import { login } from "@/api/user";
import { UserFilled, Lock, ArrowLeft } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";

const router = useRouter();
const userStore = useUserStore();
const formRef = ref();
const loading = ref(false);

const formData = reactive({
  username: "",
  password: "",
});

const rules = {
  username: [{ required: true, message: "请输入管理员账号", trigger: "blur" }],
  password: [{ required: true, message: "请输入管理员密码", trigger: "blur" }],
};

const handleLogin = async () => {
  try {
    await formRef.value.validate();
    loading.value = true;
    const res = await login(formData);

    // 检查是否为管理员
    if (res.data.role !== "admin") {
      ElMessage.error("您不是管理员，无权访问");
      loading.value = false;
      return;
    }

    // 保存用户信息
    userStore.setUser(res.data, res.data.token);
    ElMessage.success("登录成功");
    router.push("/admin");
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.admin-login-page {
  display: flex;
  min-height: 100vh;
}

.login-left {
  flex: 55;
  background: linear-gradient(135deg, #434343 0%, #000000 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
  position: relative;
  overflow: hidden;
}

.login-left::before {
  content: "";
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(
    circle,
    rgba(102, 126, 234, 0.15) 0%,
    transparent 70%
  );
  animation: rotate 30s linear infinite;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.login-decoration {
  position: relative;
  z-index: 1;
  text-align: center;
  color: #ffffff;
}

.slogan {
  font-size: 48px;
  font-weight: 700;
  margin-bottom: 20px;
  text-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
  letter-spacing: 2px;
}

.sub-slogan {
  font-size: 28px;
  font-weight: 400;
  opacity: 0.9;
  margin-bottom: 40px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
}

.decoration-icon {
  font-size: 120px;
  margin-top: 40px;
  opacity: 0.3;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-20px);
  }
}

.login-right {
  flex: 45;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
}

.login-form-container {
  width: 100%;
  max-width: 400px;
}

.login-title {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 12px;
  background: linear-gradient(135deg, #434343 0%, #667eea 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  text-align: center;
}

.login-subtitle {
  font-size: 14px;
  color: var(--text-secondary);
  text-align: center;
  margin-bottom: 40px;
}

.login-form {
  margin-top: 40px;
}

.login-form :deep(.el-input__wrapper) {
  height: 48px;
  box-shadow: 0 0 0 1px var(--border-color) inset;
  border-radius: 8px;
}

.login-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--primary-light) inset;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--primary-color) inset !important;
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  margin-top: 24px;
  background: linear-gradient(135deg, #434343 0%, #667eea 100%);
  border: none;
  font-weight: 600;
  letter-spacing: 2px;
}

.login-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
}

.login-footer :deep(.el-link) {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

@media (max-width: 768px) {
  .login-left {
    display: none;
  }

  .login-right {
    flex: 1;
    padding: 30px 20px;
  }
}
</style>
