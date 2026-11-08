<template>
  <div class="book-list-page">
    <!-- 筛选区域 -->
    <div class="filter-section card-base">
      <div class="filter-left">
        <el-input
          v-model="queryParams.keyword"
          placeholder="搜索书名或作者"
          clearable
          style="width: 300px"
          @change="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>

        <el-select
          v-model="queryParams.categoryId"
          placeholder="选择分类"
          clearable
          style="width: 150px"
          @change="handleSearch"
        >
          <el-option label="全部分类" :value="null" />
          <el-option
            v-for="category in categoryList"
            :key="category.id"
            :label="category.name"
            :value="category.id"
          />
        </el-select>

        <el-button :icon="Search" type="primary" @click="handleSearch">
          搜索
        </el-button>
        <el-button :icon="Refresh" @click="handleReset">重置</el-button>
      </div>

      <div class="filter-right">
        <el-button :icon="Plus" type="primary" @click="handleAdd">
          添加图书
        </el-button>
      </div>
    </div>

    <!-- 表格 -->
    <div class="table-container card-base">
      <el-table :data="bookList" v-loading="loading" stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" />

        <el-table-column prop="bookName" label="书名" min-width="180" />

        <el-table-column prop="author" label="作者" width="120" />

        <el-table-column prop="isbn" label="ISBN" width="130" />

        <el-table-column prop="categoryName" label="分类" width="100" />

        <el-table-column prop="publisher" label="出版社" width="150" />

        <el-table-column prop="price" label="价格" width="80">
          <template #default="{ row }">
            <span class="price-text">¥{{ row.price || "0.00" }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="stock" label="库存" width="70">
          <template #default="{ row }">
            <el-tag v-if="row.stock > 0" type="success" size="small">
              {{ row.stock }}
            </el-tag>
            <el-tag v-else type="danger" size="small">0</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? "上架" : "下架" }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              :icon="Edit"
              type="primary"
              link
              size="small"
              @click="handleEdit(row.id)"
            >
              编辑
            </el-button>
            <el-button
              :icon="Delete"
              type="danger"
              link
              size="small"
              @click="handleDelete(row.id)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="fetchBookList"
          @size-change="fetchBookList"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { useRouter } from "vue-router";
import { getBookList, deleteBook } from "@/api/book";
import { getCategoryList } from "@/api/category";
import { Search, Refresh, Plus, Edit, Delete } from "@element-plus/icons-vue";
import { ElMessage, ElMessageBox } from "element-plus";

const router = useRouter();
const loading = ref(false);
const bookList = ref([]);
const categoryList = ref([]);
const total = ref(0);

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: "",
  categoryId: null,
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

// 重置
const handleReset = () => {
  queryParams.keyword = "";
  queryParams.categoryId = null;
  queryParams.pageNum = 1;
  fetchBookList();
};

// 添加
const handleAdd = () => {
  router.push("/admin/book/add");
};

// 编辑
const handleEdit = (id) => {
  router.push(`/admin/book/edit/${id}`);
};

// 删除
const handleDelete = (id) => {
  ElMessageBox.confirm("删除后不可恢复，确定要删除该图书吗？", "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(async () => {
      try {
        await deleteBook(id);
        ElMessage.success("删除成功");
        fetchBookList();
      } catch (error) {
        console.error(error);
      }
    })
    .catch(() => {});
};

onMounted(() => {
  fetchCategoryList();
  fetchBookList();
});
</script>

<style scoped>
.book-list-page {
  padding-bottom: 40px;
}

.filter-section {
  padding: 20px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-left {
  display: flex;
  gap: 16px;
  align-items: center;
  flex-wrap: wrap;
}

.filter-right {
  flex-shrink: 0;
}

.table-container {
  padding: 20px;
}

.price-text {
  color: var(--danger-color);
  font-weight: 600;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid var(--border-light);
}

@media (max-width: 768px) {
  .filter-section {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }

  .filter-left {
    flex-direction: column;
  }

  .filter-left .el-input,
  .filter-left .el-select {
    width: 100% !important;
  }
}
</style>
