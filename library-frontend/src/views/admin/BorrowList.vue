<template>
  <div class="borrow-list-page">
    <!-- 状态筛选 -->
    <div class="status-tabs">
      <div
        class="status-tab"
        :class="{ active: queryParams.status === '' }"
        @click="handleStatusChange('')"
      >
        全部
      </div>
      <div
        class="status-tab"
        :class="{ active: queryParams.status === 'pending' }"
        @click="handleStatusChange('pending')"
      >
        <el-badge
          :value="pendingCount"
          :hidden="pendingCount === 0"
          type="danger"
        >
          待审核
        </el-badge>
      </div>
      <div
        class="status-tab"
        :class="{ active: queryParams.status === 'borrowing' }"
        @click="handleStatusChange('borrowing')"
      >
        借阅中
      </div>
      <div
        class="status-tab"
        :class="{ active: queryParams.status === 'returning' }"
        @click="handleStatusChange('returning')"
      >
        <el-badge
          :value="returningCount"
          :hidden="returningCount === 0"
          type="warning"
        >
          还书中
        </el-badge>
      </div>
      <div
        class="status-tab"
        :class="{ active: queryParams.status === 'returned' }"
        @click="handleStatusChange('returned')"
      >
        已归还
      </div>
      <div
        class="status-tab"
        :class="{ active: queryParams.status === 'rejected' }"
        @click="handleStatusChange('rejected')"
      >
        已拒绝
      </div>
    </div>

    <!-- 表格 -->
    <div class="table-container card-base">
      <el-table :data="borrowList" v-loading="loading" stripe>
        <el-table-column type="index" label="序号" width="60" />

        <el-table-column prop="username" label="学号" width="100" />

        <el-table-column prop="realName" label="借阅人" width="100" />

        <el-table-column prop="bookName" label="图书名称" min-width="180" />

        <el-table-column prop="borrowTime" label="借阅时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.borrowTime) }}
          </template>
        </el-table-column>

        <el-table-column prop="dueTime" label="应还时间" width="160">
          <template #default="{ row }">
            <span :class="{ 'overdue-text': isOverdue(row) }">
              {{ formatDate(row.dueTime) }}
            </span>
            <el-tag
              v-if="isOverdue(row)"
              type="danger"
              size="small"
              class="ml-2"
            >
              已逾期
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" effect="light" round>
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <!-- 待审核：通过/拒绝 -->
            <template v-if="row.status === 'pending'">
              <el-button
                type="success"
                size="small"
                @click="handleApprove(row.id)"
              >
                通过
              </el-button>
              <el-button
                type="danger"
                size="small"
                @click="handleReject(row.id)"
              >
                拒绝
              </el-button>
            </template>

            <!-- 还书中：确认还书 -->
            <template v-else-if="row.status === 'returning'">
              <el-button
                type="primary"
                size="small"
                @click="handleConfirm(row.id)"
              >
                确认还书
              </el-button>
            </template>

            <!-- 借阅中：提示 -->
            <span v-else-if="row.status === 'borrowing'" class="info-text">
              借阅中...
            </span>

            <!-- 其他状态：显示状态 -->
            <span v-else class="info-text">
              {{ getStatusText(row.status) }}
            </span>
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
          @current-change="fetchBorrowList"
          @size-change="fetchBorrowList"
        />
      </div>
    </div>

    <!-- 拒绝原因对话框 -->
    <el-dialog v-model="showRejectDialog" title="拒绝申请" width="450px">
      <el-input
        v-model="rejectRemark"
        type="textarea"
        :rows="4"
        placeholder="请输入拒绝原因（选填）"
      />

      <template #footer>
        <el-button @click="showRejectDialog = false">取消</el-button>
        <el-button
          type="danger"
          @click="confirmReject"
          :loading="rejectLoading"
        >
          确定拒绝
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from "vue";
import {
  getAllBorrowList,
  approveBorrow, // 审核通过API
  rejectBorrow,  // 审核拒绝API
  confirmReturn,
} from "@/api/borrow";
import { ElMessage, ElMessageBox } from "element-plus";

const loading = ref(false);
const rejectLoading = ref(false);
const borrowList = ref([]);
const total = ref(0);
const showRejectDialog = ref(false);
const rejectRemark = ref("");
const currentRejectId = ref(null);

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  status: "",
});

// 待审核和还书中的数量（用于Badge提示）
const pendingCount = computed(() => {
  return borrowList.value.filter((item) => item.status === "pending").length;
});

const returningCount = computed(() => {
  return borrowList.value.filter((item) => item.status === "returning").length;
});

// 获取借阅列表
const fetchBorrowList = async () => {
  loading.value = true;
  try {
    const params = { ...queryParams };
    if (params.status === "") {
      delete params.status;
    }
    const res = await getAllBorrowList(params);
    borrowList.value = res.data.records;
    total.value = res.data.total;
  } catch (error) {
    console.error(error);
    ElMessage.error("获取借阅列表失败，请稍后重试");
  } finally {
    loading.value = false;
  }
};

// 切换状态
const handleStatusChange = (status) => {
  queryParams.status = status;
  queryParams.pageNum = 1;
  fetchBorrowList();
};

// 审核通过
const handleApprove = (id) => {
  ElMessageBox.confirm('确定要通过该借阅申请吗？通过后图书库存将减少', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(async () => {
    try {
      await approveBorrow(id); // 调用api接口
      ElMessage.success('审核通过');
      fetchBorrowList(); // 修复：使用正确的列表刷新函数
    } catch (error) {
      ElMessage.error(error.response?.data?.message || "审核通过失败");
    }
  }).catch(() => {});
};

// 打开拒绝弹窗
const handleReject = (id) => {
  currentRejectId.value = id;
  showRejectDialog.value = true;
};

// 确认拒绝（修复：删除重复的try-catch块，修正列表刷新函数）
const confirmReject = async () => {
  // 1. 校验记录ID是否存在
  if (!currentRejectId.value) {
    ElMessage.warning("未选择要拒绝的借阅申请");
    return;
  }
  // 开启拒绝操作的加载状态
  rejectLoading.value = true; 
  try {
    // 2. 调用拒绝API
    await rejectBorrow(currentRejectId.value, rejectRemark.value);
    ElMessage.success('已拒绝申请');
    showRejectDialog.value = false;
    fetchBorrowList(); // 修复：使用正确的列表刷新函数
    rejectRemark.value = ""; // 清空输入框
  } catch (err) {
    // 3. 捕获异常并提示错误
    ElMessage.error(err.response?.data?.message || "拒绝申请失败，请稍后重试");
  } finally {
    // 无论请求成功/失败，都关闭加载状态
    rejectLoading.value = false;
  }
};

// 确认还书
const handleConfirm = (id) => {
  ElMessageBox.confirm("确定要确认还书吗？确认后图书库存将增加。", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "primary",
  })
    .then(async () => {
      try {
        await confirmReturn(id);
        ElMessage.success("还书已确认");
        fetchBorrowList();
      } catch (error) {
        console.error(error);
        ElMessage.error("确认还书失败，请稍后重试");
      }
    })
    .catch(() => {});
};

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    pending: "info",
    borrowing: "primary",
    returning: "warning",
    returned: "success",
    rejected: "danger",
  };
  return typeMap[status] || "info";
};

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    pending: "待审核",
    borrowing: "借阅中",
    returning: "还书中",
    returned: "已归还",
    rejected: "已拒绝",
  };
  return textMap[status] || status;
};

// 格式化日期（修复：增加无效日期处理）
const formatDate = (dateStr) => {
  if (!dateStr) return "-";
  const date = new Date(dateStr);
  // 检查日期是否有效
  if (isNaN(date.getTime())) return "-";
  return date.toLocaleString("zh-CN", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
};

// 判断是否逾期
const isOverdue = (row) => {
  if (!row.dueTime || row.status !== "borrowing") return false;
  return new Date(row.dueTime) < new Date();
};

onMounted(() => {
  fetchBorrowList();
});
</script>

<style scoped>
.borrow-list-page {
  padding-bottom: 40px;
}

/* 状态标签 */
.status-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 24px;
  padding: 6px;
  background: #f0f2f5;
  border-radius: 12px;
  width: fit-content;
}

.status-tab {
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  color: var(--text-regular);
  font-weight: 500;
}

.status-tab:hover {
  background: rgba(255, 255, 255, 0.5);
}

.status-tab.active {
  background: #ffffff;
  color: var(--primary-color);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.table-container {
  padding: 20px;
}

.overdue-text {
  color: var(--danger-color);
  font-weight: 600;
}

.ml-2 {
  margin-left: 8px;
}

.info-text {
  font-size: 13px;
  color: var(--text-secondary);
  font-style: italic;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid var(--border-light);
}

@media (max-width: 768px) {
  .status-tabs {
    width: 100%;
  }

  .status-tab {
    padding: 8px 14px;
    font-size: 13px;
  }
}
</style>