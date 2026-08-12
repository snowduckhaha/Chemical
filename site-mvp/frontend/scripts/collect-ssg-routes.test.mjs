import assert from "node:assert/strict";
import { createServer } from "node:http";
import test from "node:test";
import { collectRoutes } from "./collect-ssg-routes.mjs";

const fixture = {
  "/products/categories?lang=zh": [{ slug: "ath" }],
  "/products/categories/ath/series?lang=zh": [{ slug: "fine" }],
  "/products/categories/ath/series/fine/products?lang=zh": [{ slug: "ath-100" }],
  "/applications?lang=zh": [{ slug: "coatings" }],
  "/news/categories?lang=zh": [{ slug: "industry" }],
  "/news?lang=zh": [{ slug: "launch", categorySlug: "industry" }],
  "/products/categories?lang=en": [{ slug: "ath" }],
  "/products/categories/ath/series?lang=en": [{ slug: "fine" }],
  "/products/categories/ath/series/fine/products?lang=en": [{ slug: "ath-100" }],
  "/applications?lang=en": [{ slug: "coatings" }],
  "/news/categories?lang=en": [{ slug: "industry" }],
  "/news?lang=en": [{ slug: "launch", categorySlug: "industry" }]
};

async function withServer(resolver, fn) {
  const server = createServer((request, response) => {
    const data = resolver(request.url);
    response.setHeader("content-type", "application/json");
    response.end(JSON.stringify(data));
  });
  await new Promise((resolve) => server.listen(0, "127.0.0.1", resolve));
  try {
    const address = server.address();
    return await fn(`http://127.0.0.1:${address.port}`);
  } finally {
    await new Promise((resolve, reject) => server.close((error) => error ? reject(error) : resolve()));
  }
}

test("collectRoutes creates only public concrete routes", async () => {
  await withServer((url) => ({ code: 0, data: fixture[url] }), async (baseUrl) => {
    const { routes } = await collectRoutes({ baseUrl });
    assert.deepEqual(routes.filter((route) => route.includes("ath-100")), [
      "/en/products/ath/fine/ath-100", "/zh/products/ath/fine/ath-100"
    ]);
    assert.equal(routes.some((route) => route.includes("admin") || route.includes("search") || route.includes("success")), false);
  });
});

test("collectRoutes rejects malformed public API data", async () => {
  await withServer((url) => url.includes("products/categories") ? { code: 0, data: [{ slug: "" }] } : { code: 0, data: [] }, async (baseUrl) => {
    await assert.rejects(() => collectRoutes({ baseUrl }), /empty slug/);
  });
});
