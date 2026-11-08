<template>
  <div class="admin-reservation-page">
    <div class="page-header">
      <h2>预约管理</h2>
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

      <el-button :icon="Refresh" @click="handleReset">重置</el-button>
    </div>

    <div class="table-container card-base">
      <el-table :data="reservationList" v-loading="loading" stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" />

        <el-table-column label="用户信息" min-width="150">
          <template #default="{ row }">
            <div class="user-info">
              <div class="user-name">{{ row.realName || row.username }}</div>
              <div class="user-id">@{{ row.username }}</div>
            </div>
          </template>
        </el-table-column>

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

        <el-table-column label="可借时间" width="170">
          <template #default="{ row }">
            <span v-if="row.availableTime">{{ formatTime(row.availableTime) }}</span>
            <span v-else>-</span>
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

        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'waiting' || row.status === 'available'"
              type="danger"
              link
              size="small"
              @click="handleCancel(row)"
            >
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          @current-change="fetchReservationList"
          @size-change="fetchReservationList"
        />
      </div>
    </div>

    <el-dialog
      v-model="cancelDialogVisible"
      title="取消预约"
      width="450px"
    >
      <el-form label-position="top">
        <el-form-item label="取消原因（可选）">
          <el-input
            v-model="cancelRemark"
            type="textarea"
            :rows="3"
            placeholder="请输入取消原因"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelDialogVisible = false">返回</el-button>
        <el-button type="danger" :loading="loading" @click="submitCancel">
          确定取消
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { getAllReservationList, adminCancelReservation } from "@/api/reservation";
import { Refresh } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";

const loading = ref(false);
const reservationList = ref([]);
const total = ref(0);
const cancelDialogVisible = ref(false);
const currentReservation = ref({});
const cancelRemark = ref("");

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  status: null,
});

const fetchReservationList = async () => {
  loading.value = true;
  try {
    const res = await getAllReservationList(queryParams);
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

const handleCancel = (row) => {
  currentReservation.value = row;
  cancelRemark.value = "";
  cancelDialogVisible.value = true;
};

const submitCancel = async () => {
  try {
    loading.value = true;
    await adminCancelReservation(currentReservation.value.id, cancelRemark.value);
    ElMessage.success("取消预约成功");
    cancelDialogVisible.value = false;
    fetchReservationList();
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};

const getStatusType = (status) => {
  const typeMap = {
    waiting: "info",
    available: "success",
    converted: "primary",
    cancelled: "info",
    expired: "warning"
  };
  return typeMap[status] || "info";
};

const isExpired = (expireTime) => {
  return new Date(expireTime) < new Date();
};

const formatTime = (time) => {
  if (!time) return "-";
  return new Date(time).toLocaleString("zh-CN");
};

onMounted(() => {
  fetchReservationList();
});
</script>

<style scoped>
.admin-reservation-page {
  padding-bottom: 40px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
}

.filter-section {
  padding: 16px 20px;
  margin-bottom: 20px;
  display: flex;
  gap: 16px;
  align-items: center;
}

.table-container {
  padding: 20px;
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-name {
  font-weight: 500;
  color: var(--text-primary);
}

.user-id {
  font-size: 12px;
  color: var(--text-secondary);
}

.book-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.book-cover {
  width: 40px;
  height: 56px;
  border-radius: 4px;
  object-fit: cover;
}

.book-detail {
  flex: 1;
}

.book-name {
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.book-author {
  font-size: 12px;
  color: var(--text-secondary);
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid var(--border-light);
}

.expired-warning {
  color: var(--danger-color);
  font-weight: 500;
}
</style>
