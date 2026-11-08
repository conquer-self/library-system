import { createRouter, createWebHistory } from "vue-router";

const routes = [
  // 用户端路由
  {
    path: "/login",
    name: "Login",
    component: () => import("@/views/user/Login.vue"),
  },
  {
    path: "/register",
    name: "Register",
    component: () => import("@/views/user/Register.vue"),
  },
  {
    path: "/",
    component: () => import("@/views/layout/UserLayout.vue"),
    children: [
      {
        path: "",
        name: "Home",
        component: () => import("@/views/user/Home.vue"),
      },
      {
        path: "book/:id",
        name: "BookDetail",
        component: () => import("@/views/user/BookDetail.vue"),
      },
      {
        path: "my-borrow",
        name: "MyBorrow",
        component: () => import("@/views/user/MyBorrow.vue"),
        meta: { requiresAuth: true },
      },
      {
        path: "profile",
        name: "Profile",
        component: () => import("@/views/user/Profile.vue"),
        meta: { requiresAuth: true },
      },
      {
        path: "my-reservation",
        name: "MyReservation",
        component: () => import("@/views/user/MyReservation.vue"),
        meta: { requiresAuth: true },
      },
    ],
  },
  // 管理端路由
  {
    path: "/admin/login",
    name: "AdminLogin",
    component: () => import("@/views/admin/Login.vue"),
  },
  {
    path: "/admin",
    component: () => import("@/views/layout/AdminLayout.vue"),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: "",
        name: "Dashboard",
        component: () => import("@/views/admin/Dashboard.vue"),
      },
      {
        path: "book",
        name: "BookList",
        component: () => import("@/views/admin/BookList.vue"),
      },
      {
        path: "book/add",
        name: "BookAdd",
        component: () => import("@/views/admin/BookForm.vue"),
      },
      {
        path: "book/edit/:id",
        name: "BookEdit",
        component: () => import("@/views/admin/BookForm.vue"),
      },
      {
        path: "category",
        name: "CategoryList",
        component: () => import("@/views/admin/CategoryList.vue"),
      },
      {
        path: "user",
        name: "UserList",
        component: () => import("@/views/admin/UserList.vue"),
      },
      {
        path: "borrow",
        name: "BorrowList",
        component: () => import("@/views/admin/BorrowList.vue"),
      },
      {
        path: "reservation",
        name: "AdminReservation",
        component: () => import("@/views/admin/AdminReservation.vue"),
      },
    ],
  },
];

const router = createRouter({
  history: createWebHistory("/library-system/"),
  routes,
});

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem("token");
  const role = localStorage.getItem("role");

  if (to.meta.requiresAuth && !token) {
    if (to.path.startsWith("/admin")) {
      next("/admin/login");
    } else {
      next("/login");
    }
    return;
  }

  if (to.meta.requiresAdmin && role !== "admin") {
    next("/admin/login");
    return;
  }

  next();
});

export default router;
