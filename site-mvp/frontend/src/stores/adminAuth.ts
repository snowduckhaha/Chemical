import { defineStore } from "pinia";
import { adminCsrf, adminLogin, adminLogout, adminMe, type AdminSession } from "../api/site";

export const useAdminAuthStore = defineStore("adminAuth", {
  state: () => ({
    session: null as AdminSession | null,
    checked: false
  }),
  getters: {
    isAdmin: (state) => state.session?.role === "ADMIN",
    isAuthenticated: (state) => state.session !== null
  },
  actions: {
    async restore() {
      if (this.checked) return;
      try {
        this.session = await adminMe();
        await adminCsrf();
      } catch {
        this.session = null;
      } finally {
        this.checked = true;
      }
    },
    async login(username: string, password: string) {
      this.session = await adminLogin(username, password);
      await adminCsrf();
      this.checked = true;
    },
    async logout() {
      try {
        await adminLogout();
      } finally {
        this.session = null;
        this.checked = true;
      }
    }
  }
});