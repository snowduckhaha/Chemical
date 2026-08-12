const fs = require('fs');
const path = require('path');
const { marked } = require('marked');

// 配置
const MD_FILE = path.resolve('D:/Zelin/Chemical/用户使用手册.md');
const HTML_FILE = path.resolve('D:/Zelin/Chemical/用户使用手册.html');

function imageToBase64(imagePath) {
  const fullPath = path.resolve(path.dirname(MD_FILE), imagePath);
  if (!fs.existsSync(fullPath)) {
    console.warn(`  警告：图片不存在 ${fullPath}`);
    return null;
  }
  const data = fs.readFileSync(fullPath);
  const ext = path.extname(fullPath).toLowerCase();
  const mimeMap = { '.png': 'image/png', '.jpg': 'image/jpeg', '.jpeg': 'image/jpeg', '.webp': 'image/webp', '.gif': 'image/gif' };
  const mime = mimeMap[ext] || 'image/png';
  return `data:${mime};base64,${data.toString('base64')}`;
}

function processImages(html) {
  return html.replace(/<img([^>]*)src="([^"]*)"([^>]*)>/g, (match, pre, src, post) => {
    const b64 = imageToBase64(src);
    if (b64) {
      return `<img${pre}src="${b64}"${post} style="max-width:100%;height:auto;display:block;margin:1em auto;border:1px solid #ddd;">`;
    }
    return match;
  });
}

function main() {
  console.log('Step 1: Markdown -> HTML');
  console.log(`  读取: ${MD_FILE}`);
  
  if (!fs.existsSync(MD_FILE)) {
    console.error(`错误：找不到 Markdown 文件 ${MD_FILE}`);
    process.exit(1);
  }

  const mdText = fs.readFileSync(MD_FILE, 'utf-8');
  const htmlBody = marked.parse(mdText);
  const htmlWithImages = processImages(htmlBody);

  const fullHtml = `<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<title>起点化工企业官网及后台管理系统 用户使用手册</title>
<style>
@page { size: A4; margin: 2cm; }
body {
  font-family: "Segoe UI", "PingFang SC", "Microsoft YaHei", sans-serif;
  font-size: 11pt;
  line-height: 1.7;
  color: #333;
  max-width: 210mm;
  margin: 0 auto;
  padding: 20px;
}
h1 {
  font-size: 22pt;
  color: #1a5276;
  border-bottom: 3px solid #1a5276;
  padding-bottom: 10px;
  margin-top: 40px;
}
h2 {
  font-size: 16pt;
  color: #2874a6;
  border-left: 4px solid #2874a6;
  padding-left: 12px;
  margin-top: 30px;
}
h3 {
  font-size: 13pt;
  color: #333;
  margin-top: 20px;
}
table {
  border-collapse: collapse;
  width: 100%;
  margin: 1em 0;
  font-size: 10pt;
}
th, td {
  border: 1px solid #ccc;
  padding: 8px 10px;
  text-align: left;
}
th {
  background-color: #f0f4f8;
  font-weight: 600;
}
tr:nth-child(even) {
  background-color: #fafbfc;
}
code {
  background: #f4f4f4;
  padding: 2px 6px;
  border-radius: 3px;
  font-family: Consolas, monospace;
  font-size: 10pt;
}
pre {
  background: #f8f9fa;
  padding: 12px;
  border-radius: 6px;
  overflow-x: auto;
  border: 1px solid #e1e4e8;
}
blockquote {
  border-left: 4px solid #dfe2e5;
  padding-left: 16px;
  margin-left: 0;
  color: #666;
}
img {
  max-width: 100%;
  height: auto;
}
hr {
  border: none;
  border-top: 1px solid #ddd;
  margin: 30px 0;
}
a {
  color: #2874a6;
  text-decoration: none;
}
ul, ol {
  padding-left: 24px;
}
li {
  margin: 4px 0;
}
</style>
</head>
<body>
${htmlWithImages}
</body>
</html>`;

  fs.writeFileSync(HTML_FILE, fullHtml, 'utf-8');
  console.log(`  输出: ${HTML_FILE}`);
  console.log('  Markdown -> HTML 完成！');
}

main();
