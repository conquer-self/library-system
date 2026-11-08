<template>
  <div class="home-page">
    <!-- 搜索区域 -->
    <div class="search-section card-base">
      <el-input
        v-model="queryParams.keyword"
        placeholder="🔍 搜索书名或作者..."
        size="large"
        clearable
        class="search-input"
        @change="handleSearch"
      >
        <template #append>
          <el-button :icon="Search" @click="handleSearch">搜索</el-button>
        </template>
      </el-input>

      <!-- 分类筛选标签 -->
      <div class="category-tags">
        <div
          class="category-tag"
          :class="{ active: queryParams.categoryId === null }"
          @click="handleCategoryChange(null)"
        >
          全部
        </div>
        <div
          v-for="category in categoryList"
          :key="category.id"
          class="category-tag"
          :class="{ active: queryParams.categoryId === category.id }"
          @click="handleCategoryChange(category.id)"
        >
          {{ category.name }}
        </div>
      </div>
    </div>

    <!-- 图书列表 -->
    <div v-loading="loading" class="book-section">
      <el-row :gutter="24" v-if="bookList.length > 0">
        <el-col
          v-for="book in bookList"
          :key="book.id"
          :xs="12"
          :sm="8"
          :md="6"
          :lg="6"
          :xl="6"
        >
          <div class="book-card" @click="goToDetail(book.id)">
            <div class="book-cover">
              <img
                :src="
                  book.coverImg ||
                  'https://via.placeholder.com/240x320?text=No+Cover'
                "
                :alt="book.bookName"
              />
              <div class="book-overlay">
                <el-button type="primary" :icon="View" circle />
              </div>
            </div>
            <div class="book-info">
              <h3 class="book-title text-ellipsis" :title="book.bookName">
                {{ book.bookName }}
              </h3>
              <p class="book-author text-ellipsis" :title="book.author">
                {{ book.author || "未知作者" }}
              </p>
              <div class="book-footer">
                <span class="book-price">¥{{ book.price || "0.00" }}</span>
                <el-tag v-if="book.stock > 0" type="success" size="small" round>
                  库存 {{ book.stock }}
                </el-tag>
                <el-tag v-else type="danger" size="small" round>已借完</el-tag>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 空状态 -->
      <el-empty v-else-if="!loading" description="暂无图书" :image-size="120" />
    </div>

    <!-- 分页 -->
    <div class="pagination-container" v-if="total > 0">
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        :page-sizes="[8, 12, 16, 24]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="fetchBookList"
        @size-change="fetchBookList"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { useRouter } from "vue-router";
import { getBookList } from "@/api/book";
import { getCategoryList } from "@/api/category";
import { Search, View } from "@element-plus/icons-vue";

const router = useRouter();
const loading = ref(false);
const bookList = ref([]);
const categoryList = ref([]);
const total = ref(0);

const queryParams = reactive({
  pageNum: 1,
  pageSize: 12,
  keyword: "",
  categoryId: null,
  status: 1, // 只显示上架的图书
});

// 获取分类列表
const fetchCategoryList = async () => {
  try {
    const res = await getCategoryList();
    categoryList.value = res.data;
  } catch (error) {
    console.error(error);
  }
};

// 获取图书列表
const fetchBookList = async () => {
  loading.value = true;
  try {
    const res = await getBookList(queryParams);
    bookList.value = res.data.records;
    total.value = res.data.total;
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  queryParams.pageNum = 1;
  fetchBookList();
};

// 切换分类
const handleCategoryChange = (categoryId) => {
  queryParams.categoryId = categoryId;
  queryParams.pageNum = 1;
  fetchBookList();
};

// 跳转详情
const goToDetail = (id) => {
  router.push(`/book/${id}`);
};

onMounted(() => {
  fetchCategoryList();
  fetchBookList();
});
</script>

<style scoped>
.home-page {
  padding-bottom: 40px;
}

/* 搜索区域 */
.search-section {
  padding: 24px;
  margin-bottom: 32px;
}

.search-input {
  max-width: 600px;
}

.search-input :deep(.el-input__wrapper) {
  height: 48px;
  border-radius: 24px 0 0 24px;
}

.search-input :deep(.el-input-group__append) {
  border-radius: 0 24px 24px 0;
  background: var(--primary-color);
  color: #fff;
  border: none;
  box-shadow: none;
}

.search-input :deep(.el-input-group__append .el-button) {
  color: #fff;
}

/* 分类标签 */
.category-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 20px;
}

.category-tag {
  padding: 8px 20px;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
  background: #f0f2f5;
  color: var(--text-regular);
  user-select: none;
}

.category-tag:hover {
  background: var(--primary-light);
  color: #ffffff;
  transform: translateY(-2px);
}

.category-tag.active {
  background: var(--primary-color);
  color: #ffffff;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

/* 图书区域 */
.book-section {
  min-height: 400px;
}

/* 图书卡片 */
.book-card {
  background: #ffffff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid var(--border-light);
  margin-bottom: 24px;
}

.book-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.12);
  border-color: var(--primary-light);
}

.book-cover {
  position: relative;
  width: 100%;
  height: 240px;
  overflow: hidden;
  background: #f0f2f5;
}

.book-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.book-card:hover .book-cover img {
  transform: scale(1.1);
}

.book-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.book-card:hover .book-overlay {
  opacity: 1;
}

.book-info {
  padding: 16px;
}

.book-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
  line-height: 1.4;
}

.book-author {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 12px;
}

.book-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.book-price {
  font-size: 18px;
  font-weight: 600;
  color: var(--danger-color);
}

/* 分页 */
.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 32px;
  padding: 20px 0;
}

/* 响应式 */
@media (max-width: 768px) {
  .search-input {
    max-width: 100%;
  }

  .category-tags {
    gap: 8px;
  }

  .category-tag {
    padding: 6px 14px;
    font-size: 13px;
  }

  .book-cover {
    height: 180px;
  }
}
</style>
