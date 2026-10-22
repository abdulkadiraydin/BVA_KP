<#
Allure Report 2 icin tek bir arsivlenmis test kosusunun HTML raporunu uretir.
Son raporun history bilgisini sonraki kosuya aktarir.
Kullanim (proje kok klasorunde):
  .\scripts\Generate-AllureReport.ps1
    .\scripts\Generate-AllureReport.ps1 -RunDirectory '.\reports\allure-runs\Kosu_...'
Gereksinim: Allure CLI 2.x, PATH'te `allure` komutu.
#>
param(
    [string]$RunDirectory
)

$ErrorActionPreference = 'Stop'
$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$runsRoot = Join-Path $projectRoot 'reports\allure-runs'
$historyStore = Join-Path $projectRoot 'reports\allure-history'

if (-not (Test-Path $runsRoot -PathType Container)) {
    throw "Kosu klasoru bulunamadi: $runsRoot"
}

if ([string]::IsNullOrWhiteSpace($RunDirectory)) {
    $latest = Get-ChildItem $runsRoot -Directory |
        Where-Object { $_.Name -like 'Kosu_*' } |
        Sort-Object Name -Descending |
        Select-Object -First 1
    if ($null -eq $latest) { throw 'Kosu klasoru bulunamadi.' }
    $RunDirectory = $latest.FullName
}

$runPath = (Resolve-Path $RunDirectory).Path
$resultsPath = Join-Path $runPath 'allure-results'
$reportPath = Join-Path $runPath 'allure-report'
$marker = Join-Path $runPath '.allure-history-generated'

if (-not (Test-Path $resultsPath -PathType Container)) {
    throw "Allure results klasoru bulunamadi: $resultsPath"
}
if ((@(Get-ChildItem $resultsPath -Filter '*-result.json' -File)).Count -eq 0) {
    throw "Bu kosuya ait Allure result.json bulunamadi: $resultsPath"
}
if (Test-Path $marker) {
    throw 'Bu kosu icin history daha once olusturulmus. Cift history kaydi olusmamasi icin durduruldu.'
}

$allureCommand = Get-Command allure -ErrorAction SilentlyContinue
if ($null -eq $allureCommand) {
    # IntelliJ, PATH degistirilmeden once acilmissa eski PATH'i miras alabilir.
    # Windows kullanici PATH'ini bu process icin yeniden ekle (kalici degisiklik yok).
    $userPath = [Environment]::GetEnvironmentVariable('Path', 'User')
    if (-not [string]::IsNullOrWhiteSpace($userPath)) {
        $env:Path = $env:Path + ';' + $userPath
    }
    $allureCommand = Get-Command allure -ErrorAction SilentlyContinue
}
if ($null -eq $allureCommand) {
    throw 'Allure CLI bulunamadi. Allure Report 2.x kurup allure --version ile dogrulayin.'
}
$allureVersion = (& $allureCommand.Source --version | Out-String).Trim()
if ($LASTEXITCODE -ne 0 -or $allureVersion -notmatch '\b2\.\d+') {
    throw "Bu script Allure 2.x gerektiriyor. Tespit edilen: $allureVersion"
}

$historyInput = Join-Path $resultsPath 'history'
if (Test-Path $historyStore -PathType Container) {
    if (Test-Path $historyInput) { Remove-Item $historyInput -Recurse -Force }
    Copy-Item -Path $historyStore -Destination $historyInput -Recurse -Force
    Write-Host 'Onceki kosularin Allure History bilgisi eklendi.'
}

& $allureCommand.Source generate $resultsPath '--clean' '-o' $reportPath
if ($LASTEXITCODE -ne 0 -or -not (Test-Path (Join-Path $reportPath 'index.html'))) {
    throw 'Allure HTML raporu olusturulamadi; history yenilenmedi.'
}

$newHistory = Join-Path $reportPath 'history'
if (-not (Test-Path $newHistory -PathType Container)) {
    throw 'Allure raporu olustu ama history klasoru bulunamadi; history yenilenmedi.'
}

$tempHistory = Join-Path $projectRoot ('reports\allure-history-staging-' + [guid]::NewGuid().ToString('N'))
Copy-Item $newHistory $tempHistory -Recurse -Force
$backupHistory = Join-Path $projectRoot ('reports\allure-history-backup-' + [guid]::NewGuid().ToString('N'))
$hadOldHistory = Test-Path $historyStore
if ($hadOldHistory) { Move-Item $historyStore $backupHistory }
try {
    Move-Item $tempHistory $historyStore
} catch {
    if ($hadOldHistory -and -not (Test-Path $historyStore)) {
        Move-Item $backupHistory $historyStore
    }
    throw
}
if ($hadOldHistory -and (Test-Path $backupHistory)) {
    Remove-Item $backupHistory -Recurse -Force
}
Set-Content -Path $marker -Value (Get-Date -Format o) -Encoding UTF8

# Cift tiklayinca acilan tek dosyalik rapor:
#   kosu klasorunde RAPOR.html, ayrica reports\SON-KOSU.html (her kosuda uzerine yazilir).
# History bunu etkilemez; uretilemezse ana rapor yine gecerlidir.
$singleFileTemp = Join-Path $runPath '.rapor-gecici'
& $allureCommand.Source generate $resultsPath '--clean' '--single-file' '-o' $singleFileTemp
$singleFileIndex = Join-Path $singleFileTemp 'index.html'

if ($LASTEXITCODE -eq 0 -and (Test-Path $singleFileIndex)) {
    $raporHtml = Join-Path $runPath 'RAPOR.html'
    Move-Item $singleFileIndex $raporHtml -Force
    Copy-Item $raporHtml (Join-Path $projectRoot 'reports\SON-KOSU.html') -Force
    Remove-Item $singleFileTemp -Recurse -Force
    Write-Host "Rapor (cift tikla): $raporHtml"
} else {
    Write-Warning 'Tek dosyalik Allure raporu olusturulamadi; ana rapor gecerli.'
}

Write-Host "Allure HTML: $reportPath"
Write-Host "History saklandi: $historyStore"
Write-Host "Acma: allure open `"$reportPath`""
