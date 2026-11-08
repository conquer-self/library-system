<template>
  <div class="user-layout">
    <!-- 顶部导航 -->
    <header class="header">
      <div class="header-content">
        <div class="logo" @click="router.push('/')">📚 图书借阅系统</div>

        <nav class="nav-menu">
          <router-link to="/" class="nav-link" active-class="active"
            >首页</router-link
          >
          <router-link
            to="/my-borrow"
            class="nav-link"
            active-class="active"
            v-if="isLoggedIn"
            >我的借阅</router-link
          >
          <router-link
            to="/my-reservation"
            class="nav-link"
            active-class="active"
            v-if="isLoggedIn"
            >我的预约</router-link
          >
          <router-link
            to="/profile"
            class="nav-link"
            active-class="active"
            v-if="isLoggedIn"
            >个人中心</router-link
          >
        </nav>

        <div class="header-right">
          <template v-if="isLoggedIn">
            <el-dropdown>
              <span class="user-info">
                <el-avatar :size="32">{{
                  displayName.charAt(0).toUpperCase()
                }}</el-avatar>
                <span class="username">{{ displayName }}</span>
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="router.push('/profile')">
                    <el-icon><User /></el-icon>
                    个人中心
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button @click="router.push('/login')">登录</el-button>
            <el-button type="primary" @click="router.push('/register')"
              >注册</el-button
            >
          </template>
        </div>
      </div>
    </header>

    <!-- 内容区 -->
    <main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="fade-slide" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
  </div>
</template>

<script setup>
import { computed } from "vue";
import { useRouter } from "vue-router";
import { useUserStore } from "@/stores/user";
import { ArrowDown, User, SwitchButton } from "@element-plus/icons-vue";
import { ElMessageBox } from "element-plus";

const router = useRouter();
const userStore = useUserStore();

const isLoggedIn = computed(() => userStore.isLoggedIn);
const displayName = computed(
  () => userStore.userInfo?.realName || userStore.userInfo?.username || "用户"
);

const handleLogout = () => {
  ElMessageBox.confirm("确定要退出登录吗？", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(() => {
      userStore.clearUser();
      router.push("/login");
    })
    .catch(() => {});
};
</script>

<style scoped>
.user-layout {
  min-height: 100vh;
  background: var(--bg-color);
}

.header {
  height: 64px;
  background: #ffffff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  height: 100%;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  font-size: 20px;
  font-weight: 700;
  cursor: pointer;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  user-select: none;
}

.nav-menu {
  display: flex;
  gap: 8px;
}

.nav-link {
  padding: 8px 16px;
  border-radius: 6px;
  color: var(--text-regular);
  font-size: 15px;
  transition: all 0.2s;
}

.nav-link:hover,
.nav-link.active {
  color: var(--primary-color);
  background: rgba(102, 126, 234, 0.1);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 8px;
  transition: background 0.2s;
}

.user-info:hover {
  background: var(--bg-hover);
}

.username {
  font-size: 14px;
  color: var(--text-regular);
  font-weight: 500;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  min-height: calc(100vh - 64px);
}

@media (max-width: 768px) {
  .nav-menu {
    display: none;
  }

  .header-content {
    padding: 0 16px;
  }

  .main-content {
    padding: 16px;
  }

  .username {
    display: none;
  }
}
</style>
