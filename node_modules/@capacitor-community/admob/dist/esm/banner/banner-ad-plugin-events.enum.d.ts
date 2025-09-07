export declare enum BannerAdPluginEvents {
    SizeChanged = "bannerAdSizeChanged",
    Loaded = "bannerAdLoaded",
    FailedToLoad = "bannerAdFailedToLoad",
    /**
     * Open "Adsense" Event after user click banner
     */
    Opened = "bannerAdOpened",
    /**
     * Close "Adsense" Event after user click banner
     */
    Closed = "bannerAdClosed",
    /**
     * Similarly, this method should be called when an impression is recorded for the ad by the mediated SDK.
     */
    AdImpression = "bannerAdImpression"
}
