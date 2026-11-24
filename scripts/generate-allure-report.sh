#!/usr/bin/env bash
# Linux/macOS karsiligi: scripts/Generate-AllureReport.ps1
#
# Allure Report 2 icin tek bir arsivlenmis test kosusunun raporlarini uretir:
#   <kosu>/RAPOR.html            tek dosyalik rapor (cift tikla)
#   <kosu>/teknik/allure-report  allure open ile acilan rapor
#   reports/SON-KOSU.html        en son kosunun RAPOR.html kopyasi
# Son raporun history bilgisini sonraki kosuya aktarir.
# Kullanim (proje kok klasorunde):
#   ./scripts/generate-allure-report.sh
#   ./scripts/generate-allure-report.sh --run-directory reports/kosular/2026-10-22_19-41-05_HATALI
# Gereksinim: Allure CLI 2.x, PATH'te `allure` komutu.

set -euo pipefail

fail() {
  echo "HATA: $*" >&2
  exit 1
}

run_directory=""
while [[ $# -gt 0 ]]; do
  case "$1" in
    --run-directory | -RunDirectory)
      [[ $# -ge 2 ]] || fail "$1 bir deger bekliyor."
      run_directory="$2"
      shift 2
      ;;
    -h | --help)
      sed -n '2,12p' "$0"
      exit 0
      ;;
    *)
      fail "Bilinmeyen arguman: $1"
      ;;
  esac
done

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
project_root="$(cd "$script_dir/.." && pwd)"
runs_root="$project_root/reports/kosular"
history_store="$project_root/reports/allure-history"

[[ -d "$runs_root" ]] || fail "Kosu klasoru bulunamadi: $runs_root"

if [[ -z "$run_directory" ]]; then
  # Ad tarihle basladigi icin alfabetik sira kronolojik siradir.
  latest=""
  for dir in "$runs_root"/*/; do
    name="$(basename "$dir")"
    if [[ "$name" =~ ^[0-9]{4}-[0-9]{2}-[0-9]{2}_[0-9]{2}-[0-9]{2}-[0-9]{2} ]] && [[ "$name" > "$latest" ]]; then
      latest="$name"
    fi
  done
  [[ -n "$latest" ]] || fail "Kosu klasoru bulunamadi."
  run_directory="$runs_root/$latest"
fi

[[ -d "$run_directory" ]] || fail "Kosu klasoru bulunamadi: $run_directory"
run_path="$(cd "$run_directory" && pwd)"
teknik_path="$run_path/teknik"
results_path="$teknik_path/allure-results"
report_path="$teknik_path/allure-report"
marker="$teknik_path/.allure-history-generated"

[[ -d "$results_path" ]] || fail "Allure results klasoru bulunamadi: $results_path"
if ! compgen -G "$results_path/*-result.json" >/dev/null; then
  fail "Bu kosuya ait Allure result.json bulunamadi: $results_path"
fi
if [[ -e "$marker" ]]; then
  fail 'Bu kosu icin history daha once olusturulmus. Cift history kaydi olusmamasi icin durduruldu.'
fi

allure_bin="$(command -v allure || true)"
[[ -n "$allure_bin" ]] || fail 'Allure CLI bulunamadi. Allure Report 2.x kurup allure --version ile dogrulayin.'
allure_version="$("$allure_bin" --version 2>&1 | tr -d '\r' | tail -n 1)"
[[ "$allure_version" =~ (^|[^0-9])2\.[0-9]+ ]] || fail "Bu script Allure 2.x gerektiriyor. Tespit edilen: $allure_version"

history_input="$results_path/history"
if [[ -d "$history_store" ]]; then
  rm -rf "$history_input"
  cp -R "$history_store" "$history_input"
  echo 'Onceki kosularin Allure History bilgisi eklendi.'
fi

if ! "$allure_bin" generate "$results_path" --clean -o "$report_path" || [[ ! -f "$report_path/index.html" ]]; then
  fail 'Allure HTML raporu olusturulamadi; history yenilenmedi.'
fi

new_history="$report_path/history"
[[ -d "$new_history" ]] || fail 'Allure raporu olustu ama history klasoru bulunamadi; history yenilenmedi.'

# History'yi once gecici klasore kopyala, sonra eskisinin yerine tasi; yarida kalirsa eskisi geri gelir.
suffix="$(date +%s)-$$"
temp_history="$project_root/reports/allure-history-staging-$suffix"
backup_history="$project_root/reports/allure-history-backup-$suffix"
cp -R "$new_history" "$temp_history"
had_old_history=false
if [[ -e "$history_store" ]]; then
  had_old_history=true
  mv "$history_store" "$backup_history"
fi
if ! mv "$temp_history" "$history_store"; then
  if $had_old_history && [[ ! -e "$history_store" ]]; then
    mv "$backup_history" "$history_store"
  fi
  fail 'History tasinamadi.'
fi
if $had_old_history && [[ -e "$backup_history" ]]; then
  rm -rf "$backup_history"
fi
date -u +%Y-%m-%dT%H:%M:%SZ >"$marker"

# Cift tiklayinca acilan tek dosyalik rapor:
#   kosu klasorunde RAPOR.html, ayrica reports/SON-KOSU.html (her kosuda uzerine yazilir).
# History bunu etkilemez; uretilemezse ana rapor yine gecerlidir.
single_file_temp="$teknik_path/.rapor-gecici"
if "$allure_bin" generate "$results_path" --clean --single-file -o "$single_file_temp" \
  && [[ -f "$single_file_temp/index.html" ]]; then
  rapor_html="$run_path/RAPOR.html"
  mv -f "$single_file_temp/index.html" "$rapor_html"
  cp -f "$rapor_html" "$project_root/reports/SON-KOSU.html"
  rm -rf "$single_file_temp"
  echo "Rapor (cift tikla): $rapor_html"
else
  echo 'UYARI: Tek dosyalik Allure raporu olusturulamadi; ana rapor gecerli.' >&2
fi

echo "Allure HTML: $report_path"
echo "History saklandi: $history_store"
echo "Acma: allure open \"$report_path\""