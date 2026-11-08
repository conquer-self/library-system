<template>
  <div class="admin-layout">
    <!-- 侧边栏 -->
    <aside class="sidebar" :class="{ collapsed: isCollapsed }">
      <div class="sidebar-logo">
        <span v-if="!isCollapsed">📚 图书管理系统</span>
        <span v-else>📚</span>
      </div>

      <el-menu
        :default-active="currentPath"
        :collapse="isCollapsed"
        background-color="#1f2937"
        text-color="#a1a7b3"
        active-text-color="#ffffff"
        router
      >
        <el-menu-item index="/admin">
          <el-icon><DataLine /></el-icon>
          <template #title>控制台</template>
        </el-menu-item>
        <el-menu-item index="/admin/book">
          <el-icon><Reading /></el-icon>
          <template #title>图书管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/category">
          <el-icon><Menu /></el-icon>
          <template #title>分类管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/user">
          <el-icon><User /></el-icon>
          <template #title>读者管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/borrow">
          <el-icon><Tickets /></el-icon>
          <template #title>借阅管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/reservation">
          <el-icon><Clock /></el-icon>
          <template #title>预约管理</template>
        </el-menu-item>
      </el-menu>
    </aside>

    <!-- 右侧内容 -->
    <div class="main-container">
      <!-- 顶部栏 -->
      <header class="header">
        <div class="header-left">
          <el-icon
            class="collapse-btn"
            @click="isCollapsed = !isCollapsed"
            :size="20"
          >
            <Fold v-if="!isCollapsed" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/admin' }"
              >首页</el-breadcrumb-item
            >
            <el-breadcrumb-item v-if="currentTitle">{{
              currentTitle
            }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <el-dropdown>
            <span class="admin-info">
              <el-avatar :size="32" style="background-color: #667eea">{{
                adminName.charAt(0).toUpperCase()
              }}</el-avatar>
              <span class="admin-name">{{ adminName }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
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
  </div>
</template>

<script setup>
import { ref, computed } from "vue";
import { useRouter, useRoute } from "vue-router";
import { useUserStore } from "@/stores/user";
import {
  DataLine,
  Reading,
  Menu,
  User,
  Tickets,
  Fold,
  Expand,
  ArrowDown,
  SwitchButton,
  Clock,
} from "@element-plus/icons-vue";
import { ElMessageBox } from "element-plus";

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

const isCollapsed = ref(false);
const currentPath = computed(() => route.path);
const adminName = computed(() => userStore.userInfo?.realName || "管理员");

const titleMap = {
  "/admin": "",
  "/admin/book": "图书管理",
  "/admin/book/add": "添加图书",
  "/admin/category": "分类管理",
  "/admin/user": "读者管理",
  "/admin/borrow": "借阅管理",
};

const currentTitle = computed(() => {
  // 处理编辑页面
  if (route.path.startsWith("/admin/book/edit/")) {
    return "编辑图书";
  }
  return titleMap[route.path] || "";
});

const handleLogout = () => {
  ElMessageBox.confirm("确定要退出登录吗？", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(() => {
      userStore.clearUser();
      router.push("/admin/login");
    })
    .catch(() => {});
};
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: var(--bg-color);
}

.sidebar {
  width: 200px;
  background: #1f2937;
  transition: width 0.3s;
  flex-shrink: 0;
}

.sidebar.collapsed {
  width: 64px;
}

.sidebar-logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 16px;
  font-weight: 600;
  background: rgba(255, 255, 255, 0.05);
  white-space: nowrap;
  overflow: hidden;
}

.sidebar :deep(.el-menu) {
  border-right: none;
}

.sidebar :deep(.el-menu-item) {
  margin: 4px 8px;
  border-radius: 8px;
  height: 50px;
}

.sidebar :deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.08) !important;
}

.sidebar :deep(.el-menu-item.is-active) {
  background: #667eea !important;
  color: #ffffff !important;
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.header {
  height: 60px;
  background: #ffffff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  cursor: pointer;
  color: var(--text-regular);
  transition: color 0.2s;
}

.collapse-btn:hover {
  color: var(--primary-color);
}

.header-right {
  display: flex;
  align-items: center;
}

.admin-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 8px;
  transition: background 0.2s;
}

.admin-info:hover {
  background: var(--bg-hover);
}

.admin-name {
  font-size: 14px;
  color: var(--text-regular);
  font-weight: 500;
}

.main-content {
  flex: 1;
  padding: 20px;
  background: var(--bg-color);
  overflow-y: auto;
}

@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    left: 0;
    top: 0;
    bottom: 0;
    z-index: 1000;
  }

  .sidebar.collapsed {
    transform: translateX(-100%);
  }

  .admin-name {
    display: none;
  }
}
</style>
