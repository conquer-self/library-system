<template>
  <div class="book-form-page">
    <div class="page-header">
      <h1 class="page-title">{{ isEdit ? "编辑图书" : "添加图书" }}</h1>
      <el-button @click="router.back()">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
    </div>

    <div class="form-container card-base">
      <el-form
        :model="formData"
        :rules="rules"
        ref="formRef"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="书名" prop="bookName">
              <el-input v-model="formData.bookName" placeholder="请输入书名" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="作者" prop="author">
              <el-input v-model="formData.author" placeholder="请输入作者" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="ISBN" prop="isbn">
              <el-input v-model="formData.isbn" placeholder="请输入ISBN号" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="分类" prop="categoryId">
              <el-select
                v-model="formData.categoryId"
                placeholder="请选择分类"
                style="width: 100%"
              >
                <el-option
                  v-for="category in categoryList"
                  :key="category.id"
                  :label="category.name"
                  :value="category.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="出版社" prop="publisher">
              <el-input
                v-model="formData.publisher"
                placeholder="请输入出版社"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="出版日期" prop="publishDate">
              <el-date-picker
                v-model="formData.publishDate"
                type="date"
                placeholder="请选择出版日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="价格" prop="price">
              <el-input-number
                v-model="formData.price"
                :precision="2"
                :min="0"
                :max="9999"
                placeholder="请输入价格"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="库存" prop="stock">
              <el-input-number
                v-model="formData.stock"
                :min="0"
                :max="9999"
                placeholder="请输入库存"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="总数量" prop="total">
              <el-input-number
                v-model="formData.total"
                :min="0"
                :max="9999"
                placeholder="请输入总数量"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="封面图片" prop="coverImg">
          <el-input
            v-model="formData.coverImg"
            placeholder="请输入封面图片URL"
          />
          <div v-if="formData.coverImg" class="cover-preview">
            <img :src="formData.coverImg" alt="封面预览" />
          </div>
        </el-form-item>

        <el-form-item label="图书简介" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="4"
            placeholder="请输入图书简介"
          />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">上架</el-radio>
            <el-radio :label="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">
            保存
          </el-button>
          <el-button @click="router.back()">取消</el-button>
          <el-button v-if="formData.bookName" @click="handleReset"
            >重置</el-button
          >
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import { getBookDetail, addBook, updateBook } from "@/api/book";
import { getCategoryList } from "@/api/category";
import { ArrowLeft } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";

const router = useRouter();
const route = useRoute();
const formRef = ref();
const loading = ref(false);
const categoryList = ref([]);

const isEdit = ref(false);
const bookId = ref(null);

const formData = reactive({
  bookName: "",
  author: "",
  isbn: "",
  categoryId: null,
  publisher: "",
  publishDate: "",
  price: null,
  stock: 0,
  total: 0,
  coverImg: "",
  description: "",
  status: 1,
});

const rules = {
  bookName: [{ required: true, message: "请输入书名", trigger: "blur" }],
  categoryId: [{ required: true, message: "请选择分类", trigger: "change" }],
  stock: [{ required: true, message: "请输入库存", trigger: "blur" }],
  total: [{ required: true, message: "请输入总数量", trigger: "blur" }],
};

// 获取分类列表
const fetchCategoryList = async () => {
  try {
    const res = await getCategoryList();
    categoryList.value = res.data;
  } catch (error) {
    console.error(error);
  }
};

// 获取图书详情（编辑模式）
const fetchBookDetail = async () => {
  try {
    const res = await getBookDetail(bookId.value);
    Object.assign(formData, res.data);
  } catch (error) {
    console.error(error);
  }
};

// 提交
const handleSubmit = async () => {
  try {
    await formRef.value.validate();
    loading.value = true;

    if (isEdit.value) {
      await updateBook({ ...formData, id: bookId.value });
      ElMessage.success("修改成功");
    } else {
      await addBook(formData);
      ElMessage.success("添加成功");
    }

    router.back();
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};

// 重置
const handleReset = () => {
  if (isEdit.value) {
    fetchBookDetail();
  } else {
    formRef.value.resetFields();
  }
};

onMounted(() => {
  // 判断是添加还是编辑
  if (route.params.id) {
    isEdit.value = true;
    bookId.value = route.params.id;
    fetchBookDetail();
  }

  fetchCategoryList();
});
</script>

<style scoped>
.book-form-page {
  max-width: 1000px;
  margin: 0 auto;
  padding-bottom: 40px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.form-container {
  padding: 30px;
}

.cover-preview {
  margin-top: 12px;
  width: 160px;
  height: 220px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid var(--border-light);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.cover-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

@media (max-width: 768px) {
  .form-container {
    padding: 20px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>
