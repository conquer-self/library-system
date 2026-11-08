import request from "./index";

// 用户登录
export function login(data) {
  return request.post("/user/login", data);
}

// 用户注册
export function register(data) {
  return request.post("/user/register", data);
}

// 获取用户信息
export function getUserInfo() {
  return request.get("/user/info");
}

// 修改用户信息
export function updateUserInfo(data) {
  return request.put("/user/update", data);
}

// 修改密码
export function updatePassword(data) {
  return request.put("/user/password", data);
}

// 用户列表（管理员）
export function getUserList(params) {
  return request.get("/user/list", { params });
}

// 修改用户状态（管理员）
export function updateUserStatus(id, status) {
  return request.put(`/user/status/${id}`, null, { params: { status } });
}
