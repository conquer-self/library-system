<template>
  <div class="my-reservation-page">
    <div class="page-header">
      <h2>我的预约</h2>
    </div>

    <div class="filter-section card-base">
      <el-select
        v-model="queryParams.status"
        placeholder="选择状态"
        clearable
        style="width: 150px"
        @change="handleSearch"
      >
        <el-option label="全部" :value="null" />
        <el-option label="等待中" value="waiting" />
        <el-option label="可借阅" value="available" />
        <el-option label="已转借阅" value="converted" />
        <el-option label="已取消" value="cancelled" />
        <el-option label="已过期" value="expired" />
      </el-select>

      <el-button @click="handleReset">重置</el-button>
    </div>

    <div class="table-container card-base">
      <el-table :data="reservationList" v-loading="loading" stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" />

        <el-table-column label="图书信息" min-width="200">
          <template #default="{ row }">
            <div class="book-info">
              <el-image
                :src="row.coverImg"
                fit="cover"
                class="book-cover"
              />
              <div class="book-detail">
                <div class="book-name">{{ row.bookName }}</div>
                <div class="book-author">{{ row.author }}</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="statusText" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ row.statusText }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="预约时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.reserveTime) }}
          </template>
        </el-table-column>

        <el-table-column label="有效期至" width="170">
          <template #default="{ row }">
            <span v-if="row.expireTime" :class="{ 'expired-warning': isExpired(row.expireTime) }">
              {{ formatTime(row.expireTime) }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>

        <el-table-column label="剩余天数" width="100">
          <template #default="{ row }">
            <span v-if="row.daysRemaining !== null" :class="getDaysClass(row.daysRemaining)">
              {{ row.daysRemaining }}天
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'available'"
              type="primary"
              link
              size="small"
              @click="handleConfirm(row)"
            >
              确认借阅
            </el-button>
            <el-button
              v-if="row.status === 'waiting' || row.status === 'available'"
              type="danger"
              link
              size="small"
              @click="handleCancel(row)"
            >
              取消预约
            </el-button>
            <span v-if="row.status === 'converted'" class="text-info">
              已转借阅
            </span>
            <span v-if="row.status === 'cancelled'" class="text-muted">
              已取消
            </span>
            <span v-if="row.status === 'expired'" class="text-warning">
              已过期
            </span>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="fetchReservationList"
          @size-change="fetchReservationList"
        />
      </div>
    </div>

    <el-dialog
      v-model="confirmDialogVisible"
      title="确认借阅"
      width="400px"
    >
      <p>确定要确认借阅《{{ currentReservation.bookName }}》吗？</p>
      <p class="dialog-tip">请在3天内到图书馆完成借阅，逾期预约将自动取消。</p>
      <template #footer>
        <el-button @click="confirmDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="loading" @click="submitConfirm">
          确认借阅
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="cancelDialogVisible"
      title="取消预约"
      width="400px"
    >
      <p>确定要取消预约《{{ currentReservation.bookName }}》吗？</p>
      <template #footer>
        <el-button @click="cancelDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="loading" @click="submitCancel">
          确定取消
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { getMyReservationList, confirmBorrow, cancelReservation } from "@/api/reservation";
import { ElMessage, ElMessageBox } from "element-plus";

const loading = ref(false);
const reservationList = ref([]);
const total = ref(0);
const confirmDialogVisible = ref(false);
const cancelDialogVisible = ref(false);
const currentReservation = ref({});

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  status: null,
});

const fetchReservationList = async () => {
  loading.value = true;
  try {
    const res = await getMyReservationList(queryParams);
    reservationList.value = res.data.records;
    total.value = res.data.total;
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  queryParams.pageNum = 1;
  fetchReservationList();
};

const handleReset = () => {
  queryParams.status = null;
  queryParams.pageNum = 1;
  fetchReservationList();
};

const handleConfirm = (row) => {
  currentReservation.value = row;
  confirmDialogVisible.value = true;
};

const submitConfirm = async () => {
  try {
    loading.value = true;
    await confirmBorrow(currentReservation.value.id);
    ElMessage.success("确认借阅成功");
    confirmDialogVisible.value = false;
    fetchReservationList();
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};

const handleCancel = (row) => {
  currentReservation.value = row;
  ElMessageBox.confirm("取消预约后需要重新排队，确定要取消吗？", "提示", {
    confirmButtonText: "确定取消",
    cancelButtonText: "返回",
    type: "warning",
  }).then(async () => {
    try {
      loading.value = true;
      await cancelReservation(row.id);
      ElMessage.success("取消预约成功");
      fetchReservationList();
    } catch (error) {
      console.error(error);
    } finally {
      loading.value = false;
    }
  }).catch(() => {});
};

const getStatusType = (status) => {
  const typeMap = {
    waiting: "info",
    available: "success",
    converted: "primary",
    cancelled: "danger",
    expired: "warning",
  };
  return typeMap[status] || "info";
};

const statusTextMap = {
  waiting: "等待中",
  available: "可借阅",
  converted: "已转借阅",
  cancelled: "已取消",
  expired: "已过期",
};

const formatTime = (time) => {
  if (!time) return "-";
  return time.replace("T", " ").substring(0, 16);
};

const isExpired = (expireTime) => {
  return new Date(expireTime) < new Date();
};

const getDaysClass = (days) => {
  if (days < 0) return "text-danger";
  if (days <= 1) return "text-warning";
  return "text-success";
};

onMounted(() => {
  fetchReservationList();
});
</script>

<style scoped>
.my-reservation-page {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.filter-section {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  padding: 16px;
  border-radius: 8px;
}

.table-container {
  padding: 16px;
  border-radius: 8px;
}

.book-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.book-cover {
  width: 50px;
  height: 70px;
  border-radius: 4px;
  object-fit: cover;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.book-detail {
  flex: 1;
  min-width: 0;
}

.book-name {
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.book-author {
  font-size: 13px;
  color: var(--text-secondary);
}

.pagination-container {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.dialog-tip {
  color: var(--text-secondary);
  font-size: 13px;
  margin-top: 8px;
}

.text-info {
  color: var(--primary-color);
}

.text-muted {
  color: var(--text-secondary);
}

.text-warning {
  color: var(--warning-color);
}

.text-danger {
  color: var(--danger-color);
}

.text-success {
  color: var(--success-color);
}

.expired-warning {
  color: var(--danger-color);
  font-weight: 500;
}

.card-base {
  background: #fff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}
</style>
