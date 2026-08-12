# 一键更新用户使用手册：Markdown -> HTML -> PDF
# 用法：在项目根目录执行 .\scripts\update-manual.ps1

$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

Write-Host '========================================' -ForegroundColor Cyan
Write-Host '  用户使用手册更新工具' -ForegroundColor Cyan
Write-Host '========================================' -ForegroundColor Cyan
Write-Host ''

$mdFile = Join-Path $root '用户使用手册.md'
if (-not (Test-Path $mdFile)) {
    Write-Error "找不到 Markdown 文件: $mdFile"
    exit 1
}

Write-Host "源文件: $mdFile" -ForegroundColor Gray

# 设置 NODE_PATH，让脚本可以找到 marked 和 playwright-core
$env:NODE_PATH = Join-Path $root 'site-mvp' 'frontend' 'node_modules'

# Step 1: Markdown -> HTML
Write-Host ''
Write-Host '[1/2] Markdown -> HTML ...' -ForegroundColor Green

$md2htmlScript = Join-Path $root 'scripts' 'md2html.cjs'
& node $md2htmlScript

if ($LASTEXITCODE -ne 0) {
    Write-Error 'Markdown 转 HTML 失败'
    exit 1
}

# Step 2: HTML -> PDF
Write-Host ''
Write-Host '[2/2] HTML -> PDF ...' -ForegroundColor Green

$html2pdfScript = Join-Path $root 'scripts' 'html2pdf.cjs'
& node $html2pdfScript

if ($LASTEXITCODE -ne 0) {
    Write-Error 'HTML 转 PDF 失败'
    exit 1
}

# Done
Write-Host ''
Write-Host '========================================' -ForegroundColor Green
Write-Host '  更新完成！' -ForegroundColor Green
Write-Host '========================================' -ForegroundColor Green

$pdfFile = Join-Path $root '用户使用手册.pdf'
$htmlFile = Join-Path $root '用户使用手册.html'

Write-Host ""
Write-Host "输出文件：" -ForegroundColor Cyan
Write-Host "  PDF: $pdfFile" -ForegroundColor White
Write-Host "  HTML: $htmlFile" -ForegroundColor White

if (Test-Path $pdfFile) {
    $size = (Get-Item $pdfFile).Length
    $sizeMb = [math]::Round($size / 1MB, 2)
    Write-Host "  PDF 大小: $sizeMb MB" -ForegroundColor Gray
}

Write-Host ""
Write-Host "提示：你可以直接修改 .md 文件后再次运行此脚本。" -ForegroundColor Yellow
