// This enum should be keep in sync with their native equivalents with the same name
export var BannerAdPluginEvents;
(function (BannerAdPluginEvents) {
    BannerAdPluginEvents["SizeChanged"] = "bannerAdSizeChanged";
    BannerAdPluginEvents["Loaded"] = "bannerAdLoaded";
    BannerAdPluginEvents["FailedToLoad"] = "bannerAdFailedToLoad";
    /**
     * Open "Adsense" Event after user click banner
     */
    BannerAdPluginEvents["Opened"] = "bannerAdOpened";
    /**
     * Close "Adsense" Event after user click banner
     */
    BannerAdPluginEvents["Closed"] = "bannerAdClosed";
    /**
     * Similarly, this method should be called when an impression is recorded for the ad by the mediated SDK.
     */
    BannerAdPluginEvents["AdImpression"] = "bannerAdImpression";
})(BannerAdPluginEvents || (BannerAdPluginEvents = {}));
//# sourceMappingURL=banner-ad-plugin-events.enum.js.map