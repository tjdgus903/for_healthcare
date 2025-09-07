import { WebPlugin } from '@capacitor/core';
import { AdmobConsentStatus } from './consent/consent-status.enum';
export class AdMobWeb extends WebPlugin {
    async initialize() {
        console.log('initialize');
    }
    async requestTrackingAuthorization() {
        console.log('requestTrackingAuthorization');
    }
    async trackingAuthorizationStatus() {
        return {
            status: 'authorized',
        };
    }
    async requestConsentInfo(options) {
        console.log('requestConsentInfo', options);
        return {
            status: AdmobConsentStatus.REQUIRED,
            isConsentFormAvailable: true,
        };
    }
    async showConsentForm() {
        console.log('showConsentForm');
        return {
            status: AdmobConsentStatus.REQUIRED,
        };
    }
    async resetConsentInfo() {
        console.log('resetConsentInfo');
    }
    async setApplicationMuted(options) {
        console.log('setApplicationMuted', options);
    }
    async setApplicationVolume(options) {
        console.log('setApplicationVolume', options);
    }
    async showBanner(options) {
        console.log('showBanner', options);
    }
    // Hide the banner, remove it from screen, but can show it later
    async hideBanner() {
        console.log('hideBanner');
    }
    // Resume the banner, show it after hide
    async resumeBanner() {
        console.log('resumeBanner');
    }
    // Destroy the banner, remove it from screen.
    async removeBanner() {
        console.log('removeBanner');
    }
    async prepareInterstitial(options) {
        console.log('prepareInterstitial', options);
        return {
            adUnitId: options.adId,
        };
    }
    async showInterstitial() {
        console.log('showInterstitial');
    }
    async prepareRewardVideoAd(options) {
        console.log(options);
        return {
            adUnitId: options.adId,
        };
    }
    async showRewardVideoAd() {
        return {
            type: '',
            amount: 0,
        };
    }
    async prepareRewardInterstitialAd(options) {
        console.log(options);
        return {
            adUnitId: options.adId,
        };
    }
    async showRewardInterstitialAd() {
        return {
            type: '',
            amount: 0,
        };
    }
}
//# sourceMappingURL=web.js.map