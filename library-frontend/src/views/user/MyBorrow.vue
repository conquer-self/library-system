<template>
  <div class="my-borrow-page">
    <h1 class="page-title">我的借阅</h1>

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
        待审核
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
        还书中
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

    <!-- 借阅记录列表 -->
    <div v-loading="loading">
      <div
        v-for="record in borrowList"
        :key="record.id"
        class="borrow-card card-base"
      >
        <div class="book-cover">
          <img
            :src="
              record.coverImg ||
              'https://via.placeholder.com/100x140?text=No+Cover'
            "
            :alt="record.bookName"
          />
        </div>

        <div class="borrow-info">
          <div class="info-header">
            <h3 class="book-name">{{ record.bookName }}</h3>
            <el-tag :type="getStatusType(record.status)" effect="light" round>
              {{ getStatusText(record.status) }}
            </el-tag>
          </div>

          <p class="book-author">
            <el-icon><User /></el-icon>
            作者：{{ record.author || "未知" }}
          </p>

          <div class="time-info">
            <p v-if="record.borrowTime">
              <el-icon><Clock /></el-icon>
              借阅时间：{{ formatDate(record.borrowTime) }}
            </p>
            <p v-if="record.dueTime">
              <el-icon><Calendar /></el-icon>
              应还时间：{{ formatDate(record.dueTime) }}
              <el-tag v-if="isOverdue(record)" type="danger" size="small"
                >已逾期</el-tag
              >
            </p>
            <p v-if="record.returnTime">
              <el-icon><CircleCheck /></el-icon>
              归还时间：{{ formatDate(record.returnTime) }}
            </p>
            <p v-if="!record.borrowTime && record.createTime">
              <el-icon><Clock /></el-icon>
              申请时间：{{ formatDate(record.createTime) }}
            </p>
          </div>

          <p v-if="record.renewCount > 0" class="renew-info">
            <el-icon><Refresh /></el-icon>
            已续借 {{ record.renewCount }} 次
          </p>

          <p v-if="record.remark" class="remark">
            <el-icon><Warning /></el-icon>
            备注：{{ record.remark }}
          </p>
        </div>

        <div class="borrow-actions">
          <el-button
            v-if="record.status === 'borrowing'"
            type="primary"
            @click="handleReturn(record.id)"
          >
            申请还书
          </el-button>
          <el-button
            v-if="
              record.status === 'borrowing' &&
              record.renewCount < 1 &&
              !isOverdue(record)
            "
            type="success"
            @click="handleRenew(record.id)"
          >
            续借
          </el-button>
          <div v-if="record.status === 'pending'" class="waiting-text">
            等待管理员审核...
          </div>
          <div v-if="record.status === 'returning'" class="waiting-text">
            等待管理员确认还书...
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty
        v-if="!loading && borrowList.length === 0"
        description="暂无借阅记录"
        :image-size="160"
      />
    </div>

    <!-- 分页 -->
    <div class="pagination-container" v-if="total > 0">
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        :page-sizes="[5, 10, 20]"
        layout="total, sizes, prev, pager, next"
        @current-change="fetchBorrowList"
        @size-change="fetchBorrowList"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { getMyBorrowList, applyReturn, renewBorrow } from "@/api/borrow";
import {
  User,
  Clock,
  Calendar,
  CircleCheck,
  Refresh,
  Warning,
} from "@element-plus/icons-vue";
import { ElMessage, ElMessageBox } from "element-plus";

const loading = ref(false);
const borrowList = ref([]);
const total = ref(0);

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  status: "",
});

// 获取借阅列表
const fetchBorrowList = async () => {
  loading.value = true;
  try {
    const params = { ...queryParams };
    if (params.status === "") {
      delete params.status;
    }
    const res = await getMyBorrowList(params);
    borrowList.value = res.data.records;
    total.value = res.data.total;
  } catch (error) {
    console.error(error);
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

// 申请还书
const handleReturn = (id) => {
  ElMessageBox.confirm("确定要申请归还该图书吗？", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(async () => {
      try {
        await applyReturn(id);
        ElMessage.success("还书申请已提交");
        fetchBorrowList();
      } catch (error) {
        console.error(error);
      }
    })
    .catch(() => {});
};

// 续借
const handleRenew = (id) => {
  ElMessageBox.confirm("确定要续借该图书吗？续借将延长30天。", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "info",
  })
    .then(async () => {
      try {
        await renewBorrow(id);
        ElMessage.success("续借成功");
        fetchBorrowList();
      } catch (error) {
        console.error(error);
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
    overdue: "danger",
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
    overdue: "已逾期",
  };
  return textMap[status] || status;
};

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return "-";
  const date = new Date(dateStr);
  return date.toLocaleString("zh-CN", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
};

// 判断是否逾期
const isOverdue = (record) => {
  if (!record.dueTime || record.status !== "borrowing") return false;
  return new Date(record.dueTime) < new Date();
};

onMounted(() => {
  fetchBorrowList();
});
</script>

<style scoped>
.my-borrow-page {
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

/* 借阅卡片 */
.borrow-card {
  display: flex;
  gap: 20px;
  padding: 20px;
  margin-bottom: 16px;
  transition: all 0.3s;
}

.borrow-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.book-cover {
  flex-shrink: 0;
  width: 100px;
  height: 140px;
  border-radius: 8px;
  overflow: hidden;
  background: #f0f2f5;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.book-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.borrow-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}

.book-name {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.book-author {
  font-size: 14px;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: 4px;
  margin: 0;
}

.time-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.time-info p {
  font-size: 13px;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 0;
}

.time-info .el-icon {
  font-size: 14px;
}

.renew-info {
  font-size: 13px;
  color: var(--success-color);
  display: flex;
  align-items: center;
  gap: 4px;
  margin: 8px 0 0 0;
}

.remark {
  font-size: 13px;
  color: var(--warning-color);
  display: flex;
  align-items: center;
  gap: 4px;
  margin: 8px 0 0 0;
  padding: 8px 12px;
  background: #fef0e6;
  border-radius: 6px;
}

.borrow-actions {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
  justify-content: center;
  align-items: flex-end;
}

.waiting-text {
  font-size: 13px;
  color: var(--text-secondary);
  font-style: italic;
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
  .status-tabs {
    width: 100%;
    justify-content: flex-start;
  }

  .status-tab {
    padding: 8px 14px;
    font-size: 13px;
  }

  .borrow-card {
    flex-direction: column;
  }

  .book-cover {
    width: 100%;
    height: 200px;
  }

  .borrow-actions {
    flex-direction: row;
    justify-content: flex-start;
  }

  .info-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>
