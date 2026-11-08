import request from "./index";

// 分类列表
export function getCategoryList() {
  return request.get("/category/list");
}

// 添加分类
export function addCategory(data) {
  return request.post("/category/add", data);
}

// 修改分类
export function updateCategory(data) {
  return request.put("/category/update", data);
}

// 删除分类
export function deleteCategory(id) {
  return request.delete(`/category/${id}`);
}
