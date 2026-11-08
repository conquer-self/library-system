import request from "./index";

// 系统概览（管理员）
export function getOverview() {
  return request.get("/stats/overview");
}

// 热门图书TOP10（管理员）
export function getHotBooks() {
  return request.get("/stats/hot-books");
}
