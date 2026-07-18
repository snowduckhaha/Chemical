$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

$containerName = 'qidian-mysql'
$schemaScripts = @(
    '/docker-entrypoint-initdb.d/mvp_schema.sql',
    '/docker-entrypoint-initdb.d/product_center_schema_v2.sql',
    '/docker-entrypoint-initdb.d/V3__admin_auth.sql',
    '/docker-entrypoint-initdb.d/V4__admin_uploads.sql',
    '/docker-entrypoint-initdb.d/V5__product_soft_delete.sql',
    '/docker-entrypoint-initdb.d/V6__inquiry_admin.sql',
    '/docker-entrypoint-initdb.d/V7__certificate_management.sql',
    '/docker-entrypoint-initdb.d/V8__seo_admin.sql',
    '/docker-entrypoint-initdb.d/V9__analytics.sql',
    '/docker-entrypoint-initdb.d/V10__product_content_fields.sql',
    '/docker-entrypoint-initdb.d/V11__inquiry_lead_score.sql',
    '/docker-entrypoint-initdb.d/V12__news_admin.sql',
    '/docker-entrypoint-initdb.d/V13__repair_seed_media_urls.sql',
    '/docker-entrypoint-initdb.d/V14__application_field_configuration.sql',
    '/docker-entrypoint-initdb.d/V15__seed_initial_news_categories.sql',
    '/docker-entrypoint-initdb.d/V16__temporary_news_reference_assets.sql'
)

function Invoke-MySql {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Sql
    )

    docker exec -e MYSQL_PWD=root123456 $containerName mysql -uroot qidian_site -N -e $Sql
    if ($LASTEXITCODE -ne 0) {
        throw "MySQL command failed: $Sql"
    }
}

function Test-ColumnExists {
    param(
        [Parameter(Mandatory = $true)]
        [string]$TableName,
        [Parameter(Mandatory = $true)]
        [string]$ColumnName
    )

    $result = Invoke-MySql "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = 'qidian_site' AND table_name = '$TableName' AND column_name = '$ColumnName';"
    return [int]($result | Select-Object -First 1) -gt 0
}

Write-Host '[1/4] Applying schema scripts...'
foreach ($script in $schemaScripts) {
    $null = Invoke-MySql "source $script"
}

Write-Host '[2/4] Repairing legacy drift if needed...'
if (-not (Test-ColumnExists -TableName 'product' -ColumnName 'sort_order')) {
    Invoke-MySql "ALTER TABLE product ADD COLUMN sort_order INT NOT NULL DEFAULT 0 AFTER packaging_en;"
    Write-Host 'Patched legacy product table: added sort_order.'
}

foreach ($tableName in @('product_category', 'product_series', 'product')) {
    if (-not (Test-ColumnExists -TableName $tableName -ColumnName 'deleted_at')) {
        throw "Validation failed: $tableName.deleted_at is missing."
    }
}

Write-Host '[3/4] Importing seed data...'
$null = Invoke-MySql 'source /docker-entrypoint-initdb.d/mvp_seed.sql'

Write-Host '[4/4] Validating schema state...'
$tableRows = Invoke-MySql @"
SELECT expected.table_name, IF(actual.table_name IS NULL, 'MISSING', 'OK') AS status
FROM (
  SELECT 'application_field' AS table_name
        UNION ALL SELECT 'analytics_event'
        UNION ALL SELECT 'analytics_daily_aggregate'
    UNION ALL SELECT 'admin_user'
        UNION ALL SELECT 'certificate'
    UNION ALL SELECT 'product_category_image'
  UNION ALL SELECT 'application_series_link'
  UNION ALL SELECT 'content_translation_log'
  UNION ALL SELECT 'inquiry'
    UNION ALL SELECT 'inquiry_status_history'
  UNION ALL SELECT 'media_asset'
  UNION ALL SELECT 'news_article'
  UNION ALL SELECT 'news_category'
  UNION ALL SELECT 'page_content'
  UNION ALL SELECT 'product'
  UNION ALL SELECT 'product_category'
  UNION ALL SELECT 'product_image'
  UNION ALL SELECT 'product_parameter'
  UNION ALL SELECT 'product_recommend_relation'
  UNION ALL SELECT 'product_series'
  UNION ALL SELECT 'product_series_image'
  UNION ALL SELECT 'seo_meta'
) expected
LEFT JOIN information_schema.tables actual
  ON actual.table_schema = 'qidian_site'
 AND actual.table_name = expected.table_name
ORDER BY expected.table_name;
"@

$missingTables = @()
foreach ($row in $tableRows) {
    $parts = $row -split "`t"
    if ($parts.Length -ge 2) {
        $tableName = $parts[0]
        $status = $parts[1]
        Write-Host ("- {0}: {1}" -f $tableName, $status)
        if ($status -ne 'OK') {
            $missingTables += $tableName
        }
    }
}

if (-not (Test-ColumnExists -TableName 'product' -ColumnName 'sort_order')) {
    throw 'Validation failed: product.sort_order is still missing.'
}

if ($missingTables.Count -gt 0) {
    throw ('Validation failed. Missing tables: ' + ($missingTables -join ', '))
}

$productCount = Invoke-MySql 'SELECT COUNT(*) FROM product;'
$categoryCount = Invoke-MySql 'SELECT COUNT(*) FROM product_category;'

Write-Host ''
Write-Host 'Database check completed successfully.'
Write-Host ('- product_category rows: {0}' -f ($categoryCount | Select-Object -First 1))
Write-Host ('- product rows: {0}' -f ($productCount | Select-Object -First 1))