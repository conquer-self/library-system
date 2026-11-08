<template>
  <div class="book-detail-page">
    <div class="back-btn">
      <el-button @click="router.back()">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
    </div>

    <div v-loading="loading" class="detail-container card-base">
      <div class="book-main">
        <!-- 封面图 -->
        <div class="book-cover-large">
          <img
            :src="
              book.coverImg ||
              'https://via.placeholder.com/240x320?text=No+Cover'
            "
            :alt="book.bookName"
          />
        </div>

        <!-- 图书信息 -->
        <div class="book-details">
          <h1 class="book-title">{{ book.bookName }}</h1>

          <div class="info-row">
            <span class="info-label">作者：</span>
            <span class="info-value">{{ book.author || "未知" }}</span>
          </div>

          <div class="info-row">
            <span class="info-label">出版社：</span>
            <span class="info-value">{{ book.publisher || "未知" }}</span>
          </div>

          <div class="info-row">
            <span class="info-label">ISBN：</span>
            <span class="info-value">{{ book.isbn || "无" }}</span>
          </div>

          <div class="info-row">
            <span class="info-label">分类：</span>
            <el-tag v-if="book.categoryName" type="primary" size="small">
              {{ book.categoryName }}
            </el-tag>
            <span v-else class="info-value">未分类</span>
          </div>

          <div class="info-row">
            <span class="info-label">出版日期：</span>
            <span class="info-value">{{ book.publishDate || "未知" }}</span>
          </div>

          <div class="info-row">
            <span class="info-label">价格：</span>
            <span class="price-large">¥{{ book.price || "0.00" }}</span>
          </div>

          <div class="info-row">
            <span class="info-label">库存：</span>
            <el-tag v-if="book.stock > 0" type="success" size="large">
              剩余 {{ book.stock }} 本
            </el-tag>
            <el-tag v-else type="danger" size="large"> 已借完 </el-tag>
          </div>

          <!-- 借阅按钮 -->
          
            <!-- 借阅按钮区域 -->
<div class="borrow-btn-area" style="margin-top: 20px; margin-bottom: 20px;">
  <el-button
    v-if="isLoggedIn"
    type="primary"
    size="large"
    :disabled="book.stock <= 0"
    :loading="borrowing"
    @click="handleBorrow"
    class="borrow-btn"
  >
    <el-icon><Reading /></el-icon>
    {{ book.stock > 0 ? "立即借阅" : "库存不足" }}
  </el-button>
  <el-button
    v-else
    type="primary"
    size="large"
    @click="router.push('/login')"
    class="borrow-btn"
  >
    登录后借阅
  </el-button>
</div>
            <!-- TODO 增加借阅按钮 -->
          </div>
        </div>
      </div>

      <!-- 图书简介 -->
      <div class="book-description" v-if="book.description">
        <h2 class="section-title">图书简介</h2>
        <div class="description-content">
          {{ book.description }}
        </div>
      </div>
    </div>
  
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import { useUserStore } from "@/stores/user";
import { getBookDetail } from "@/api/book";
//TODO 导入借阅 API
import { applyBorrow } from "@/api/borrow";
import { ArrowLeft, Reading } from "@element-plus/icons-vue";
import { ElMessage, ElMessageBox } from "element-plus";

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

const loading = ref(false);
const borrowing = ref(false);
const book = ref({});

const isLoggedIn = computed(() => userStore.isLoggedIn);

// 获取图书详情
const fetchBookDetail = async () => {
  loading.value = true;
  try {
    const res = await getBookDetail(route.params.id);
    book.value = res.data;
  } catch (error) {
    console.error(error);
    ElMessage.error("获取图书详情失败");
  } finally {
    loading.value = false;
  }
};

// 借阅图书
// TODO 实现借阅处理函数
// 借阅图书
const handleBorrow = () => {
  // TODO 1: 使用 ElMessageBox.confirm 弹出确认框
  ElMessageBox.confirm(
    `确定要借阅《${book.value.bookName}》吗？`,
    "提示",
    {
      confirmButtonText: "确定借阅",
      cancelButtonText: "取消",
      type: "info"
    }
  )
  // TODO 2: 在.then()回调中（使用async函数）
  .then(async () => {
    borrowing.value = true; // 显示加载状态
    try {
      // 调用applyBorrow函数，传入图书ID参数
      await applyBorrow({ bookId: book.value.id });
      // 显示成功提示
      ElMessage.success("借阅申请已提交，请等待管理员审核");
      // 刷新图书信息
      await fetchBookDetail();
    } catch (error) {
      // 输出错误信息
      console.error(error);
    } finally {
      // 关闭加载状态
      borrowing.value = false;
    }
  })
  // TODO 3: 在.catch()回调中（用户取消时不操作）
  .catch(() => {});
};

onMounted(() => {
  fetchBookDetail();
});
</script>

<style scoped>
.book-detail-page {
  max-width: 1000px;
  margin: 0 auto;
  padding-bottom: 40px;
}

.back-btn {
  margin-bottom: 20px;
}

.detail-container {
  padding: 40px;
}

.book-main {
  display: flex;
  gap: 40px;
  margin-bottom: 40px;
}

/* 封面图 */
.book-cover-large {
  flex-shrink: 0;
  width: 240px;
  height: 320px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.book-cover-large img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 图书详情 */
.book-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.book-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 12px 0;
  line-height: 1.3;
}

.info-row {
  display: flex;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid var(--border-light);
}

.info-label {
  font-size: 14px;
  color: var(--text-secondary);
  width: 100px;
  flex-shrink: 0;
}

.info-value {
  font-size: 15px;
  color: var(--text-primary);
  font-weight: 500;
}

.price-large {
  font-size: 24px;
  font-weight: 700;
  color: var(--danger-color);
}

.borrow-action {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 2px solid var(--border-light);
}

.borrow-btn {
  min-width: 200px;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
}

/* 图书简介 */
.book-description {
  padding-top: 32px;
  border-top: 2px solid var(--border-light);
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 20px 0;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-light);
}

.description-content {
  font-size: 15px;
  color: var(--text-regular);
  line-height: 1.8;
  text-align: justify;
  white-space: pre-wrap;
}

/* 响应式 */
@media (max-width: 768px) {
  .detail-container {
    padding: 20px;
  }

  .book-main {
    flex-direction: column;
    align-items: center;
    gap: 24px;
  }

  .book-cover-large {
    width: 200px;
    height: 267px;
  }

  .book-title {
    font-size: 22px;
    text-align: center;
  }

  .borrow-btn {
    width: 100%;
  }
}
</style>
