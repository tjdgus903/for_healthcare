import { WebPlugin } from '@capacitor/core';
import type { AdMobPlugin, ApplicationMutedOptions, ApplicationVolumeOptions, AdmobConsentInfo, AdmobConsentRequestOptions } from '.';
import type { AdMobRewardItem } from './reward';
import type { AdOptions, AdLoadInfo } from './shared';
import type { TrackingAuthorizationStatusInterface } from './shared/tracking-authorization-status.interface';
export declare class AdMobWeb extends WebPlugin implements AdMobPlugin {
    initialize(): Promise<void>;
    requestTrackingAuthorization(): Promise<void>;
    trackingAuthorizationStatus(): Promise<TrackingAuthorizationStatusInterface>;
    requestConsentInfo(options?: AdmobConsentRequestOptions): Promise<AdmobConsentInfo>;
    showConsentForm(): Promise<AdmobConsentInfo>;
    resetConsentInfo(): Promise<void>;
    setApplicationMuted(options: ApplicationMutedOptions): Promise<void>;
    setApplicationVolume(options: ApplicationVolumeOptions): Promise<void>;
    showBanner(options: AdOptions): Promise<void>;
    hideBanner(): Promise<void>;
    resumeBanner(): Promise<void>;
    removeBanner(): Promise<void>;
    prepareInterstitial(options: AdOptions): Promise<AdLoadInfo>;
    showInterstitial(): Promise<void>;
    prepareRewardVideoAd(options: AdOptions): Promise<AdLoadInfo>;
    showRewardVideoAd(): Promise<AdMobRewardItem>;
    prepareRewardInterstitialAd(options: AdOptions): Promise<AdLoadInfo>;
    showRewardInterstitialAd(): Promise<AdMobRewardItem>;
}
