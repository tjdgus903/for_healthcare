import { AdMob, RewardAdOptions, RewardAdPluginEvents } from '@capacitor-community/admob';
import { api } from './api';

// 보상형 광고 보여주기
export async function showRewarded(type: string, amount: number) {
  const options: RewardAdOptions = {
    adId: import.meta.env.VITE_ADMOB_REWARDED_ID, // .env에서 불러옴
    isTesting: true, // 개발 모드에서는 true (실서비스에서는 false)
  };

  await AdMob.prepareRewardAd(options);

  AdMob.addListener(RewardAdPluginEvents.Rewarded, async () => {
    console.log(`Rewarded: ${type} +${amount}`);
    // 보상 완료 후 서버에 리워드 반영
    await api("/ads/rewards/claim", {
      method: "POST",
      body: JSON.stringify({ type, amount })
    });
  });

  await AdMob.showRewardAd();
}