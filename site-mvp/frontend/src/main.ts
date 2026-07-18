import { createApp } from "vue";
import { createPinia } from "pinia";
import ElementPlus from "element-plus";
import "element-plus/dist/index.css";
import "./styles/theme.css";
import "./styles/admin-system.css";
import App from "./App.vue";
import router from "./router";
import { NavMegaDropdownPlugin } from "./plugins/navMegaDropdown";

const app = createApp(App);
app.use(createPinia());
app.use(router);
app.use(ElementPlus);
app.use(NavMegaDropdownPlugin);
app.mount("#app");
