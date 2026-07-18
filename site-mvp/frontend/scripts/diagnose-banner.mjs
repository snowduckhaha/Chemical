import { chromium } from 'playwright';
import { writeFile } from 'node:fs/promises';

const mode = process.argv[2];

const browser = await chromium.launch();
const page = await browser.newPage({ viewport: { width: 1440, height: 900 } });

try {
  if (mode === 'local-asset') {
    await page.setContent('<img id="hero" src="http://127.0.0.1:5173/products/banner-target.webp" style="width:100%;height:330px;object-fit:cover;display:block" />');
    await page.waitForFunction(() => {
      const img = document.getElementById('hero');
      return img instanceof HTMLImageElement && img.complete && img.naturalWidth > 0;
    });
    await page.locator('#hero').screenshot({ path: 'd:/Zelin/Chemical/site-mvp/frontend/direct-banner-asset.png' });
    console.log('WROTE:direct-banner-asset.png');
  } else if (mode === 'capture-target-hero') {
    await page.goto('https://www.zibopengfeng.cn/product.html?category=12', { waitUntil: 'networkidle' });
    await writeFile('d:/Zelin/Chemical/site-mvp/frontend/target-hero-status.txt', 'page-loaded', 'utf8');
    await page.screenshot({
      path: 'd:/Zelin/Chemical/site-mvp/frontend/target-hero-crop.png',
      clip: {
        x: 0,
        y: 101,
        width: 1440,
        height: 240
      }
    });
    await writeFile('d:/Zelin/Chemical/site-mvp/frontend/target-hero-status.txt', 'screenshot-written', 'utf8');
    console.log('WROTE:target-hero-crop.png');
  } else if (mode === 'target-top-assets') {
    const imageRequests = [];
    page.on('response', async (response) => {
      const request = response.request();
      if (request.resourceType() !== 'image') {
        return;
      }

      imageRequests.push({
        url: response.url(),
        status: response.status()
      });
    });

    await page.goto('https://www.zibopengfeng.cn/product.html?category=12', { waitUntil: 'networkidle' });
    const report = await page.evaluate(() => {
      const images = Array.from(document.images)
        .map((img) => ({
          src: img.currentSrc || img.src,
          width: img.naturalWidth,
          height: img.naturalHeight,
          top: img.getBoundingClientRect().top,
          visible: img.getBoundingClientRect().width > 0 && img.getBoundingClientRect().height > 0
        }))
        .filter((img) => img.visible)
        .sort((a, b) => a.top - b.top)
        .slice(0, 12);

      const bgElements = Array.from(document.querySelectorAll('*'))
        .map((node) => {
          const style = window.getComputedStyle(node);
          const rect = node.getBoundingClientRect();
          return {
            tag: node.tagName,
            className: node.className,
            top: rect.top,
            width: rect.width,
            height: rect.height,
            backgroundImage: style.backgroundImage
          };
        })
        .filter((item) => item.backgroundImage && item.backgroundImage !== 'none' && item.width > 200 && item.height > 120)
        .sort((a, b) => a.top - b.top)
        .slice(0, 12);

      return { images, bgElements };
    });

    await page.screenshot({ path: 'd:/Zelin/Chemical/site-mvp/frontend/target-top-viewport.png' });
    await writeFile(
      'd:/Zelin/Chemical/site-mvp/frontend/target-top-assets.json',
      JSON.stringify({ ...report, imageRequests }, null, 2),
      'utf8'
    );
    console.log('WROTE:target-top-assets.json');
  } else {
    throw new Error(`Unsupported mode: ${mode}`);
  }
} finally {
  await browser.close();
}