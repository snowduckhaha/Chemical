const { chromium } = require('playwright-core');
const path = require('path');

const HTML_FILE = path.resolve('D:/Zelin/Chemical/用户使用手册.html');
const PDF_FILE = path.resolve('D:/Zelin/Chemical/用户使用手册.pdf');

(async () => {
  console.log('Step 2: HTML -> PDF');
  console.log(`  读取: ${HTML_FILE}`);

  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext();
  const page = await context.newPage();

  await page.goto('file:///' + HTML_FILE.replace(/\\/g, '/'), {
    waitUntil: 'networkidle',
    timeout: 60000,
  });
  await page.waitForTimeout(2000);

  await page.pdf({
    path: PDF_FILE,
    format: 'A4',
    printBackground: true,
    margin: { top: '2cm', bottom: '2cm', left: '2cm', right: '2cm' },
    displayHeaderFooter: true,
    headerTemplate: '<div style="font-size:9px;width:100%;text-align:center;color:#999;padding-top:5px;">起点化工企业官网及后台管理系统 用户使用手册</div>',
    footerTemplate: '<div style="font-size:9px;width:100%;text-align:center;color:#999;padding-bottom:5px;"><span class="pageNumber"></span> / <span class="totalPages"></span></div>',
  });

  console.log(`  输出: ${PDF_FILE}`);
  console.log('  HTML -> PDF 完成！');

  await browser.close();
})();
