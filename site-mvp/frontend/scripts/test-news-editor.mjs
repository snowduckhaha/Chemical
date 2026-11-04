import assert from 'node:assert/strict';
import { createServer } from 'vite';
import { chromium } from 'playwright';

// Run from frontend: node scripts/test-news-editor.mjs. All API calls are mocked.
const server = await createServer({ server: { port: 0, host: '127.0.0.1', open: false } });
let browser;
try {
  await server.listen();
  browser = await chromium.launch({ headless: true, channel: process.env.TEST_BROWSER_CHANNEL || 'msedge' });
  const page = await browser.newPage();
  page.on('pageerror', error => console.error(error.message));
  let saved;
  await page.route('**/api/**', async route => {
    const path = new URL(route.request().url()).pathname;
    if (!path.startsWith('/api/')) return route.continue();
    let data = [];
    if (path.endsWith('/auth/me')) data = { username: 'test', role: 'ADMIN' };
    else if (path.endsWith('/auth/csrf')) data = { headerName: 'X-CSRF-TOKEN', token: 'test' };
    else if (path.endsWith('/categories/options')) data = [{ id: 1, name_zh: '测试分类', publish_status: 'PUBLISHED' }];
    else if (path.endsWith('/news/articles')) {
      if (route.request().method() === 'POST') { saved = route.request().postDataJSON(); data = { id: 1 }; }
      else data = { items: [], total: 0 };
    }
    await route.fulfill({ json: { code: 0, data } });
  });
  await page.goto(`${server.resolvedUrls.local[0]}scripts/news-editor.fixture.html`);
  await page.getByRole('button', { name: '新建资讯', exact: true }).click();
  await page.getByRole('button', { name: '正文与封面', exact: true }).click();
  const editors = page.locator('.editor-surface');
  for (let i = 0; i < 2; i++) {
    const editor = editors.nth(i);
    await editor.click();
    assert.equal(await editor.evaluate(el => document.activeElement === el), true, `editor ${i} loses focus after click`);
    await page.keyboard.type('abc');
    await page.keyboard.press('ArrowLeft');
    await page.keyboard.type('X');
    assert.equal(await editor.innerText(), 'abXc', 'typing must preserve the caret');
  }
  await page.getByRole('button', { name: '基础信息', exact: true }).click();
  await page.getByRole('button', { name: '正文与封面', exact: true }).click();
  assert.equal(await editors.first().innerText(), 'abXc');
  await editors.first().click();
  await page.keyboard.press('Control+End');
  await page.locator('.rich-editor').first().getByRole('button', { name: '插入表格', exact: true }).click();
  const cell = editors.first().locator('td').first();
  await cell.click();
  await page.keyboard.type('table-test');
  assert.match(await cell.innerText(), /table-test/);
  assert.equal(await editors.first().evaluate(el => document.activeElement === el), true);
  await page.getByRole('button', { name: '基础信息', exact: true }).click();
  await page.locator('select').filter({ has: page.locator('option', { hasText: '请选择' }) }).selectOption('1');
  await page.getByLabel('标题（中文） *', { exact: true }).fill('测试资讯');
  await page.getByLabel('标题（英文） *', { exact: true }).fill('Test news');
  await page.getByRole('button', { name: '保存草稿', exact: true }).click();
  await page.waitForFunction(() => !document.querySelector('.drawer-root'));
  assert.match(saved.contentZh, /table-test/);
  assert.equal(saved.contentEn, 'abXc');
  console.log('PASS: both editors retain focus, caret, tab content, table input and save payload.');
} finally {
  await browser?.close();
  await server.close();
}
