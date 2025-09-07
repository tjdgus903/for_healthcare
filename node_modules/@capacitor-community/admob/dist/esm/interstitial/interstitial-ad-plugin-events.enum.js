// This enum should be keep in sync with their native equivalents with the same name
export var InterstitialAdPluginEvents;
(function (InterstitialAdPluginEvents) {
    /**
     * Emits after trying to prepare and Interstitial, when it is loaded and ready to be show
     */
    InterstitialAdPluginEvents["Loaded"] = "interstitialAdLoaded";
    /**
     * Emits after trying to prepare and Interstitial, when it could not be loaded
     */
    InterstitialAdPluginEvents["FailedToLoad"] = "interstitialAdFailedToLoad";
    /**
     * Emits when the Interstitial ad is visible to the user
     */
    InterstitialAdPluginEvents["Showed"] = "interstitialAdShowed";
    /**
     * Emits when the Interstitial ad is failed to show
     */
    InterstitialAdPluginEvents["FailedToShow"] = "interstitialAdFailedToShow";
    /**
     * Emits when the Interstitial ad is not visible to the user anymore.
     */
    InterstitialAdPluginEvents["Dismissed"] = "interstitialAdDismissed";
})(InterstitialAdPluginEvents || (InterstitialAdPluginEvents = {}));
//# sourceMappingURL=interstitial-ad-plugin-events.enum.js.map