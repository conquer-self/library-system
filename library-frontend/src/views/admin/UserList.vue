<template>
  <div class="user-list-page">
    <!-- 筛选区域 -->
    <div class="filter-section card-base">
      <div class="filter-left">
        <el-input
          v-model="queryParams.keyword"
          placeholder="搜索学号或姓名"
          clearable
          style="width: 300px"
          @change="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>

        <el-button :icon="Search" type="primary" @click="handleSearch">
          搜索
        </el-button>
        <el-button :icon="Refresh" @click="handleReset">重置</el-button>
      </div>
    </div>

    <!-- 表格 -->
    <div class="table-container card-base">
      <el-table :data="userList" v-loading="loading" stripe>
        <el-table-column type="index" label="序号" width="60" />

        <el-table-column prop="username" label="学号/账号" width="120" />

        <el-table-column prop="realName" label="真实姓名" width="120" />

        <el-table-column prop="phone" label="手机号" width="130" />

        <el-table-column prop="email" label="邮箱" min-width="180" />

        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag
              :type="row.role === 'admin' ? 'danger' : 'primary'"
              size="small"
            >
              {{ row.role === "admin" ? "管理员" : "普通用户" }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="注册时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>

        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
            />
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
          @current-change="fetchUserList"
          @size-change="fetchUserList"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { getUserList, updateUserStatus } from "@/api/user";
import { Search, Refresh } from "@element-plus/icons-vue";
import { ElMessage, ElMessageBox } from "element-plus";

const loading = ref(false);
const userList = ref([]);
const total = ref(0);

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: "",
});

// 获取用户列表
const fetchUserList = async () => {
  loading.value = true;
  try {
    const res = await getUserList(queryParams);
    userList.value = res.data.records;
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
  fetchUserList();
};

// 重置
const handleReset = () => {
  queryParams.keyword = "";
  queryParams.pageNum = 1;
  fetchUserList();
};

// 状态切换
const handleStatusChange = (row) => {
  const statusText = row.status === 1 ? "启用" : "禁用";

  ElMessageBox.confirm(`确定要${statusText}该用户吗？`, "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(async () => {
      try {
        await updateUserStatus(row.id, row.status);
        ElMessage.success(`${statusText}成功`);
        fetchUserList();
      } catch (error) {
        // 失败时恢复原状态
        row.status = row.status === 1 ? 0 : 1;
        console.error(error);
      }
    })
    .catch(() => {
      // 取消时恢复原状态
      row.status = row.status === 1 ? 0 : 1;
    });
};

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return "-";
  return new Date(dateStr).toLocaleString("zh-CN", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
};

onMounted(() => {
  fetchUserList();
});
</script>

<style scoped>
.user-list-page {
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

.table-container {
  padding: 20px;
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

  .filter-left .el-input {
    width: 100% !important;
  }
}
</style>
