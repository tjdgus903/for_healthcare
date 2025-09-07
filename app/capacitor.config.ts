import { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.healthcare.play',
  appName: 'Healthcare',
  webDir: 'dist',   // Vite 빌드 산출물 경로
  bundledWebRuntime: false
};

export default config;