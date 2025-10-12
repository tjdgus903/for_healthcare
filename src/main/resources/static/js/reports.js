import { get, downloadFile } from '/js/frontend.esm.js';

async function loadMyReport(days = 30) {
  try {
    // 백엔드 컨트롤러: @GetMapping("/reports/me"), @RequestParam("rangeDays")
    const data = await get(`/reports/me?rangeDays=${encodeURIComponent(days)}`);

    const out = document.getElementById('out');
    if (out) out.textContent = JSON.stringify(data, null, 2);
  } catch (e) {
    console.error(e);
    const out = document.getElementById('out');
    if (out) out.textContent = `리포트 로드 실패: ${e}`;
    alert('리포트를 불러오지 못했습니다.');
  }
}

async function exportZip(days = 30) {
  try {
    // 서버에 해당 엔드포인트를 만들었을 때 사용
    // frontend.esm.js 의 downloadFile는 Authorization 헤더도 자동으로 붙여줍니다.
    await downloadFile(`/reports/export.zip?rangeDays=${encodeURIComponent(days)}`);
  } catch (e) {
    console.error(e);
    alert('내보내기 실패: ' + e);
  }
}

document.addEventListener('DOMContentLoaded', () => {
  document.getElementById('btnMyReport')?.addEventListener('click', () => loadMyReport());
  document.getElementById('btnExportZip')?.addEventListener('click', () => exportZip());

  // 페이지 진입 즉시 불러오기
  loadMyReport();
});

export { loadMyReport, exportZip };