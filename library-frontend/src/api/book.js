import request from "./index";

// 图书列表
export function getBookList(params) {
  return request.get("/book/list", { params });
}

// 图书详情
export function getBookDetail(id) {
  return request.get(`/book/${id}`);
}

// 添加图书
export function addBook(data) {
  return request.post("/book/add", data);
}

// 修改图书
export function updateBook(data) {
  return request.put("/book/update", data);
}

// 删除图书
export function deleteBook(id) {
  return request.delete(`/book/${id}`);
}
