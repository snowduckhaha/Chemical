import { existsSync, readFileSync } from "node:fs";
import { resolve } from "node:path";

const routes = JSON.parse(readFileSync(resolve("src/generated/ssg-routes.json"), "utf8"));
const dist = resolve("dist");
const missing = routes.filter((route) => {
  const file = route === "/" ? "index.html" : `${route.replace(/^\//, "")}.html`;
  return !existsSync(resolve(dist, file));
});

if (missing.length) {
  throw new Error(`SSG output is missing ${missing.length} route(s): ${missing.join(", ")}`);
}

const empty = routes.filter((route) => {
  const file = route === "/" ? "index.html" : `${route.replace(/^\//, "")}.html`;
  const html = readFileSync(resolve(dist, file), "utf8");
  return !html.includes('id="app"') || html.length < 500;
});
if (empty.length) {
  throw new Error(`SSG output is unexpectedly empty for: ${empty.join(", ")}`);
}

console.log(`Verified ${routes.length} pre-rendered public route(s).`);
