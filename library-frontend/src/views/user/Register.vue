<template>
  <div class="register-page">
    <!-- 左侧装饰区 -->
    <div class="register-left">
      <div class="register-decoration">
        <h1 class="slogan">加入我们</h1>
        <h2 class="sub-slogan">开启阅读之旅</h2>
        <div class="decoration-icon">📖</div>
      </div>
    </div>

    <!-- 右侧表单区 -->
    <div class="register-right">
      <div class="register-form-container">
        <h1 class="register-title">学生注册</h1>
        <p class="register-subtitle">创建您的图书借阅账号</p>

        <el-form
          :model="formData"
          :rules="rules"
          ref="formRef"
          class="register-form"
          label-position="top"
        >
          <el-form-item label="学号/用户名" prop="username">
            <el-input
              v-model="formData.username"
              placeholder="请输入学号或用户名"
              size="large"
              clearable
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="formData.password"
              type="password"
              placeholder="请输入密码（6位以上）"
              size="large"
              show-password
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="formData.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              size="large"
              show-password
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item label="真实姓名" prop="realName">
            <el-input
              v-model="formData.realName"
              placeholder="请输入真实姓名"
              size="large"
              clearable
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item label="手机号" prop="phone">
            <el-input
              v-model="formData.phone"
              placeholder="请输入手机号"
              size="large"
              clearable
            >
              <template #prefix>
                <el-icon><Phone /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <!-- TODO: 编写邮箱输入框 -->
<!-- 邮箱输入框 -->
<el-form-item label="邮箱" prop="email">
  <el-input
    v-model="formData.email"
    placeholder="请输入邮箱地址"
    clearable
    prefix-icon="Message"
  ></el-input>
</el-form-item>
          <el-button
            type="primary"
            size="large"
            class="register-btn"
            :loading="loading"
            @click="handleRegister"
          >
            注 册
          </el-button>
        </el-form>

        <div class="register-footer">
          <span>已有账号？</span>
          <el-link type="primary" @click="router.push('/login')"
            >立即登录</el-link
          >
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";
import { register } from "@/api/user";
import { User, Lock, Phone, Message } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";

const router = useRouter();
const formRef = ref();
const loading = ref(false);

const formData = reactive({
  username: "",
  password: "",
  confirmPassword: "",
  realName: "",
  phone: "",
  email:""
});

const validatePass = (rule, value, callback) => {
  if (value === "") {
    callback(new Error("请输入密码"));
  } else if (value.length < 6) {
    callback(new Error("密码长度不能少于6位"));
  } else {
    if (formData.confirmPassword !== "") {
      formRef.value.validateField("confirmPassword");
    }
    callback();
  }
};

const validateConfirmPass = (rule, value, callback) => {
  if (value === "") {
    callback(new Error("请再次输入密码"));
  } else if (value !== formData.password) {
    callback(new Error("两次输入密码不一致"));
  } else {
    callback();
  }
};

const validatePhone = (rule, value, callback) => {
  if (!value) {
    callback(new Error("请输入手机号"));
  } else if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error("请输入正确的手机号"));
  } else {
    callback();
  }
};

// TODO: 编写邮箱验证函数
// 自定义邮箱验证函数
const validateEmail = (rule, value, callback) => {
  // 邮箱正则表达式
  const emailReg = /^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/
  if (!value) {
    // 为空时提示
    return callback(new Error('请输入邮箱')) 
  }
  if (!emailReg.test(value)) {
    // 格式错误提示
    return callback(new Error('请输入正确的邮箱格式')) 
  }
  // 验证通过
  callback() 
}
const rules = {
  username: [
    { required: true, message: "请输入学号/用户名", trigger: "blur" },
    { min: 3, max: 20, message: "长度在3到20个字符", trigger: "blur" },
  ],
  password: [{ required: true, validator: validatePass, trigger: "blur" }],
  confirmPassword: [
    { required: true, validator: validateConfirmPass, trigger: "blur" },
  ],
  realName: [
    { required: true, message: "请输入真实姓名", trigger: "blur" },
    { min: 2, max: 10, message: "长度在2到10个字符", trigger: "blur" },
  ],
  phone: [{ required: true, validator: validatePhone, trigger: "blur" }],
  email: [
    { validator: validateEmail, trigger: 'blur' } // 失焦时触发验证
  ]
};

const handleRegister = async () => {
  try {
    await formRef.value.validate();
    loading.value = true;

    await register({
      username: formData.username,
      password: formData.password,
      realName: formData.realName,
      phone: formData.phone,
      email:formData.email
    });

    ElMessage.success("注册成功，请登录");
    router.push("/login");
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.register-page {
  display: flex;
  min-height: 100vh;
}

.register-left {
  flex: 55;
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
  position: relative;
  overflow: hidden;
}

.register-left::before {
  content: "";
  position: absolute;
  top: -50%;
  right: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(
    circle,
    rgba(255, 255, 255, 0.1) 0%,
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

.register-decoration {
  position: relative;
  z-index: 1;
  text-align: center;
  color: #ffffff;
}

.slogan {
  font-size: 48px;
  font-weight: 700;
  margin-bottom: 20px;
  text-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
  letter-spacing: 2px;
}

.sub-slogan {
  font-size: 28px;
  font-weight: 400;
  opacity: 0.95;
  margin-bottom: 40px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
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

.register-right {
  flex: 45;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 60px 40px;
  overflow-y: auto;
}

.register-form-container {
  width: 100%;
  max-width: 400px;
}

.register-title {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 12px;
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  text-align: center;
}

.register-subtitle {
  font-size: 14px;
  color: var(--text-secondary);
  text-align: center;
  margin-bottom: 40px;
}

.register-form {
  margin-top: 24px;
}

.register-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: var(--text-primary);
}

.register-form :deep(.el-input__wrapper) {
  height: 48px;
  box-shadow: 0 0 0 1px var(--border-color) inset;
  border-radius: 8px;
}

.register-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--primary-light) inset;
}

.register-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--primary-color) inset !important;
}

.register-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  margin-top: 12px;
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  border: none;
  font-weight: 600;
  letter-spacing: 2px;
}

.register-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: var(--text-secondary);
}

.register-footer span {
  margin-right: 8px;
}

@media (max-width: 768px) {
  .register-left {
    display: none;
  }

  .register-right {
    flex: 1;
    padding: 30px 20px;
  }
}
</style>
