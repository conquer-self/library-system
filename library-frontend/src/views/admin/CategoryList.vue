<template>
  <div class="category-list-page">
    <div class="page-header">
      <h1 class="page-title">分类管理</h1>
      <el-button :icon="Plus" type="primary" @click="handleAdd">
        添加分类
      </el-button>
    </div>

    <!-- 表格 -->
    <div class="table-container card-base">
      <el-table :data="categoryList" v-loading="loading" stripe>
        <el-table-column type="index" label="序号" width="80" />

        <el-table-column prop="name" label="分类名称" min-width="200" />

        <el-table-column prop="sort" label="排序号" width="120">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ row.sort || 0 }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              :icon="Edit"
              type="primary"
              link
              size="small"
              @click="handleEdit(row)"
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
    </div>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="showDialog"
      :title="isEdit ? '编辑分类' : '添加分类'"
      width="500px"
      @close="resetForm"
    >
      <el-form
        :model="formData"
        :rules="rules"
        ref="formRef"
        label-width="80px"
      >
        <el-form-item label="分类名称" prop="name">
          <el-input
            v-model="formData.name"
            placeholder="请输入分类名称"
            clearable
          />
        </el-form-item>

        <el-form-item label="排序号" prop="sort">
          <el-input-number
            v-model="formData.sort"
            :min="0"
            :max="9999"
            placeholder="数字越小越靠前"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button
          type="primary"
          @click="handleSubmit"
          :loading="submitLoading"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import {
  getCategoryList,
  addCategory,
  updateCategory,
  deleteCategory,
} from "@/api/category";
import { Plus, Edit, Delete } from "@element-plus/icons-vue";
import { ElMessage, ElMessageBox } from "element-plus";

const loading = ref(false);
const submitLoading = ref(false);
const categoryList = ref([]);
const showDialog = ref(false);
const isEdit = ref(false);
const formRef = ref();

const formData = reactive({
  id: null,
  name: "",
  sort: 0,
});

const rules = {
  name: [
    { required: true, message: "请输入分类名称", trigger: "blur" },
    { min: 2, max: 20, message: "长度在2到20个字符", trigger: "blur" },
  ],
};

// 获取分类列表
const fetchCategoryList = async () => {
  loading.value = true;
  try {
    const res = await getCategoryList();
    categoryList.value = res.data;
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};

// 添加
const handleAdd = () => {
  isEdit.value = false;
  resetForm();
  showDialog.value = true;
};

// 编辑
const handleEdit = (row) => {
  isEdit.value = true;
  formData.id = row.id;
  formData.name = row.name;
  formData.sort = row.sort || 0;
  showDialog.value = true;
};

// 提交
const handleSubmit = async () => {
  try {
    await formRef.value.validate();
    submitLoading.value = true;

    if (isEdit.value) {
      await updateCategory(formData);
      ElMessage.success("修改成功");
    } else {
      await addCategory({
        name: formData.name,
        sort: formData.sort,
      });
      ElMessage.success("添加成功");
    }

    showDialog.value = false;
    fetchCategoryList();
  } catch (error) {
    console.error(error);
  } finally {
    submitLoading.value = false;
  }
};

// 删除
const handleDelete = (id) => {
  ElMessageBox.confirm("删除后不可恢复，确定要删除该分类吗？", "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(async () => {
      try {
        await deleteCategory(id);
        ElMessage.success("删除成功");
        fetchCategoryList();
      } catch (error) {
        console.error(error);
      }
    })
    .catch(() => {});
};

// 重置表单
const resetForm = () => {
  formData.id = null;
  formData.name = "";
  formData.sort = 0;
  formRef.value?.clearValidate();
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
  fetchCategoryList();
});
</script>

<style scoped>
.category-list-page {
  padding-bottom: 40px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.table-container {
  padding: 20px;
}
</style>
