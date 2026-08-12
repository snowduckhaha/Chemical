import { defineConfig, loadEnv } from "vite";
import vue from "@vitejs/plugin-vue";

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), "");
  const proxyTarget = env.VITE_PROXY_TARGET || "http://localhost:8080";
  return {
    plugins: [vue()],
    // This token is referenced exclusively below an SSR branch in site.ts.
    // It is intentionally not a VITE_* variable.
    define: { __SSG_API_BASE__: JSON.stringify(env.SSG_API_BASE || "") },
    ssgOptions: {
      // Public detail pages issue several API reads while rendering. Keep the
      // build below the backend's connection-pool capacity.
      concurrency: 3
    },
    server: {
      port: 5173,
      proxy: {
        "/api": {
          target: proxyTarget,
          changeOrigin: true
        },
        "/uploads": {
          target: proxyTarget,
          changeOrigin: true
        }
      }
    }
  };
});
