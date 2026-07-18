import type { App } from "vue";
import NavMegaDropdown from "../components/NavMegaDropdown.vue";

export type NavMegaDropdownItem = {
  label: string;
  path: string;
};

export const NavMegaDropdownPlugin = {
  install(app: App) {
    app.component("NavMegaDropdown", NavMegaDropdown);
  }
};

export { NavMegaDropdown };
