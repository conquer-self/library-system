import request from "./index";

// 借书申请
export function applyBorrow(data) {
  return request.post("/borrow/apply", data);
}

// 还书申请
export function applyReturn(id) {
  return request.post(`/borrow/return/${id}`);
}

// 续借
export function renewBorrow(id) {
  return request.post(`/borrow/renew/${id}`);
}

// 我的借阅记录
export function getMyBorrowList(params) {
  return request.get("/borrow/my", { params });
}

// 所有借阅记录（管理员）
export function getAllBorrowList(params) {
  return request.get("/borrow/list", { params });
}

// 审核通过（管理员）
export function approveBorrow(id) {
  return request.post(`/borrow/approve/${id}`);
}

// 审核拒绝（管理员）
export function rejectBorrow(id, remark) {
  return request.post(`/borrow/reject/${id}`, null, { params: { remark } });
}

// 确认还书（管理员）
export function confirmReturn(id) {
  return request.put(`/borrow/confirm/${id}`);
}
