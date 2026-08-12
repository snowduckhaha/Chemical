import { createPinia } from "pinia";
import ElementPlus, { ID_INJECTION_KEY } from "element-plus";
import { ViteSSG } from "vite-ssg";
import "element-plus/dist/index.css";
import "./styles/theme.css";
import "./styles/admin-system.css";
import App from "./App.vue";
import { configureRouter, routes } from "./router";
import { NavMegaDropdownPlugin } from "./plugins/navMegaDropdown";
import generatedRoutes from "./generated/ssg-routes.json";

export const createApp = ViteSSG(App, {
  routes,
  scrollBehavior(to, _from, savedPosition) {
    if (to.hash) return { el: to.hash, top: 96, behavior: "auto" };
    return savedPosition || { top: 0 };
  }
}, ({ app, router, initialState, isClient, onSSRAppRendered }) => {
  const pinia = createPinia();
  app.use(pinia);
  // Element Plus otherwise generates non-deterministic SSR control ids.
  app.provide(ID_INJECTION_KEY, { prefix: 1024, current: 0 });
  app.use(ElementPlus);
  app.use(NavMegaDropdownPlugin);
  configureRouter(router, isClient);

  if (isClient) {
    pinia.state.value = initialState.pinia || {};
  } else {
    onSSRAppRendered(() => {
      initialState.pinia = pinia.state.value;
    });
  }
});

/** Only public, concrete URLs are emitted by Vite-SSG. */
export function includedRoutes() {
  return generatedRoutes;
}
