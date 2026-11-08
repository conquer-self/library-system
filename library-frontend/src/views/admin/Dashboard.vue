<template>
  <div class="dashboard-page">
    <h1 class="page-title">数据概览</h1>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <div class="stat-card stat-card-1">
          <div class="stat-content">
            <div class="stat-title">图书总数</div>
            <div class="stat-value">{{ overview.bookCount || 0 }}</div>
          </div>
          <el-icon class="stat-icon"><Reading /></el-icon>
        </div>
      </el-col>

      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <div class="stat-card stat-card-2">
          <div class="stat-content">
            <div class="stat-title">读者总数</div>
            <div class="stat-value">{{ overview.userCount || 0 }}</div>
          </div>
          <el-icon class="stat-icon"><User /></el-icon>
        </div>
      </el-col>

      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <div class="stat-card stat-card-3">
          <div class="stat-content">
            <div class="stat-title">借阅总数</div>
            <div class="stat-value">{{ overview.borrowCount || 0 }}</div>
          </div>
          <el-icon class="stat-icon"><Tickets /></el-icon>
        </div>
      </el-col>

      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <div class="stat-card stat-card-4">
          <div class="stat-content">
            <div class="stat-title">待审核数</div>
            <div class="stat-value">{{ overview.pendingCount || 0 }}</div>
          </div>
          <el-icon class="stat-icon"><Warning /></el-icon>
        </div>
      </el-col>
    </el-row>

    <!-- 热门图书 -->
    <div class="hot-books-section card-base">
      <h2 class="section-title">
        <el-icon><TrendCharts /></el-icon>
        热门借阅 TOP10
      </h2>

      <el-table :data="hotBooks" v-loading="loading" stripe>
        <el-table-column type="index" label="排名" width="80">
          <template #default="{ $index }">
            <div class="rank-badge" :class="'rank-' + ($index + 1)">
              {{ $index + 1 }}
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="bookName" label="图书名称" min-width="300" />

        <el-table-column prop="borrowCount" label="借阅次数" width="120">
          <template #default="{ row }">
            <el-tag type="success" effect="dark"
              >{{ row.borrowCount }} 次</el-tag
            >
          </template>
        </el-table-column>

        <el-table-column label="热度指数" width="200">
          <template #default="{ row }">
            <el-progress
              :percentage="getHeatPercentage(row.borrowCount)"
              :color="getHeatColor(row.borrowCount)"
              :stroke-width="10"
            />
          </template>
        </el-table-column>
      </el-table>

      <el-empty
        v-if="!loading && hotBooks.length === 0"
        description="暂无借阅数据"
        :image-size="120"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { getOverview, getHotBooks } from "@/api/stats";
import {
  Reading,
  User,
  Tickets,
  Warning,
  TrendCharts,
} from "@element-plus/icons-vue";

const loading = ref(false);
const overview = ref({
  bookCount: 0,
  userCount: 0,
  borrowCount: 0,
  pendingCount: 0,
});
const hotBooks = ref([]);
const maxBorrowCount = ref(0);

// 获取概览数据
const fetchOverview = async () => {
  try {
    const res = await getOverview();
    overview.value = res.data;
  } catch (error) {
    console.error(error);
  }
};

// 获取热门图书
const fetchHotBooks = async () => {
  loading.value = true;
  try {
    const res = await getHotBooks();
    hotBooks.value = res.data;

    // 计算最大借阅次数（用于进度条）
    if (hotBooks.value.length > 0) {
      maxBorrowCount.value = Math.max(
        ...hotBooks.value.map((b) => b.borrowCount)
      );
    }
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};

// 获取热度百分比
const getHeatPercentage = (count) => {
  if (maxBorrowCount.value === 0) return 0;
  return Math.round((count / maxBorrowCount.value) * 100);
};

// 获取热度颜色
const getHeatColor = (count) => {
  const percentage = getHeatPercentage(count);
  if (percentage >= 80) return "#f56c6c";
  if (percentage >= 60) return "#e6a23c";
  if (percentage >= 40) return "#409eff";
  return "#67c23a";
};

onMounted(() => {
  fetchOverview();
  fetchHotBooks();
});
</script>

<style scoped>
.dashboard-page {
  padding-bottom: 40px;
}

/* 统计卡片行 */
.stats-row {
  margin-bottom: 32px;
}

/* 统计卡片基础样式 */
.stat-card {
  border-radius: 16px;
  padding: 24px;
  color: #ffffff;
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  cursor: pointer;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
}

/* 4种渐变背景 - 参考UI_DESIGN.md */
.stat-card-1 {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-card-2 {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-card-3 {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-card-4 {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.stat-content {
  position: relative;
  z-index: 1;
}

.stat-title {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 12px;
  font-weight: 500;
}

.stat-value {
  font-size: 36px;
  font-weight: 700;
  line-height: 1;
}

.stat-icon {
  position: absolute;
  right: 20px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 64px;
  opacity: 0.2;
}

/* 热门图书区域 */
.hot-books-section {
  padding: 24px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 20px 0;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-light);
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 排名徽章 */
.rank-badge {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 14px;
  margin: 0 auto;
}

.rank-1 {
  background: linear-gradient(135deg, #ffd700 0%, #ffed4e 100%);
  color: #8b4513;
  box-shadow: 0 2px 8px rgba(255, 215, 0, 0.4);
}

.rank-2 {
  background: linear-gradient(135deg, #c0c0c0 0%, #e8e8e8 100%);
  color: #4a4a4a;
  box-shadow: 0 2px 8px rgba(192, 192, 192, 0.4);
}

.rank-3 {
  background: linear-gradient(135deg, #cd7f32 0%, #d4a574 100%);
  color: #ffffff;
  box-shadow: 0 2px 8px rgba(205, 127, 50, 0.4);
}

.rank-badge:not(.rank-1):not(.rank-2):not(.rank-3) {
  background: #f0f2f5;
  color: var(--text-regular);
}

@media (max-width: 768px) {
  .stat-value {
    font-size: 28px;
  }

  .stat-icon {
    font-size: 48px;
  }
}
</style>
