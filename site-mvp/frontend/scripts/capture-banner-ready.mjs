import { chromium } from 'playwright';

// Support both env vars and CLI args to avoid shell-specific env assignment issues.
const targetUrl = process.argv[2] || process.env.CAPTURE_URL;
const outputPath = process.argv[3] || process.env.CAPTURE_OUTPUT;

if (!targetUrl) {
  throw new Error('CAPTURE_URL is required');
}

if (!outputPath) {
  throw new Error('CAPTURE_OUTPUT is required');
}

const browser = await chromium.launch();
const page = await browser.newPage({ viewport: { width: 1440, height: 2200 } });

try {
  await page.goto(targetUrl, { waitUntil: 'networkidle' });
  await page.waitForSelector('.banner-card', { state: 'visible' });
  await page.waitForFunction(() => {
    const banner = document.querySelector('.banner-card');
    if (!banner) {
      return false;
    }

    if (banner instanceof HTMLImageElement) {
      return banner.complete && banner.naturalWidth > 0 && banner.clientHeight > 0;
    }

    const style = window.getComputedStyle(banner);
    return style.backgroundImage !== 'none' && banner.clientHeight > 0;
  });

  await page.screenshot({ path: outputPath, fullPage: true });
} finally {
  await browser.close();
}