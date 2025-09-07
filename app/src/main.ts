import { login } from './api';
import { showRewarded } from './ads';

// 전역 window 객체에 붙여서 HTML 버튼에서 쉽게 호출 가능
(window as any).Hybrid = {
  login,
  showRewarded
};