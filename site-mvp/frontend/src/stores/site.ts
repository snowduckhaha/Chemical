import { defineStore } from "pinia";

export const useSiteStore = defineStore("site", {
  state: () => ({
    lang: "zh" as "zh" | "en",
    companyName: "深圳市起点化工有限公司",
    companyNameEn: "Shenzhen Origin Chemical Industry Co., Ltd.",
    primaryColor: "#0b4da2"
  }),
  actions: {
    setLang(lang: "zh" | "en") {
      this.lang = lang;
    }
  }
});
