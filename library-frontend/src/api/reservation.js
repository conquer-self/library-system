import request from "./index";

export function reserveBook(data) {
  return request.post("/reservation/reserve", data);
}

export function confirmBorrow(id) {
  return request.post(`/reservation/confirm/${id}`);
}

export function cancelReservation(id) {
  return request.post(`/reservation/cancel/${id}`);
}

export function getMyReservationList(params) {
  return request.get("/reservation/my", { params });
}

export function getAllReservationList(params) {
  return request.get("/reservation/list", { params });
}

export function adminCancelReservation(id, remark) {
  return request.post(`/reservation/admin/cancel/${id}`, null, { params: { remark } });
}
