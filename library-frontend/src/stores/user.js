import { defineStore } from "pinia";
import { ref, computed } from "vue";

export const useUserStore = defineStore("user", () => {
  const userInfo = ref(JSON.parse(localStorage.getItem("userInfo") || "null"));
  const token = ref(localStorage.getItem("token") || "");

  const isLoggedIn = computed(() => !!token.value);
  const isAdmin = computed(() => userInfo.value?.role === "admin");
  const username = computed(() => userInfo.value?.username || "");

  function setUser(user, newToken) {
    userInfo.value = user;
    token.value = newToken;
    localStorage.setItem("userInfo", JSON.stringify(user));
    localStorage.setItem("token", newToken);
    localStorage.setItem("role", user.role);
  }

  function clearUser() {
    userInfo.value = null;
    token.value = "";
    localStorage.removeItem("userInfo");
    localStorage.removeItem("token");
    localStorage.removeItem("role");
  }

  return {
    userInfo,
    token,
    isLoggedIn,
    isAdmin,
    username,
    setUser,
    clearUser,
  };
});
