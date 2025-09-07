/**
 *  For more information:
 *  https://developers.google.com/admob/unity/reference/namespace/google-mobile-ads/ump/api#consentstatus
 *
 * */
export var AdmobConsentStatus;
(function (AdmobConsentStatus) {
    /**
     * User consent not required.
     */
    AdmobConsentStatus["NOT_REQUIRED"] = "NOT_REQUIRED";
    /**
     * User consent already obtained.
     */
    AdmobConsentStatus["OBTAINED"] = "OBTAINED";
    /**
     * User consent required but not yet obtained.
     */
    AdmobConsentStatus["REQUIRED"] = "REQUIRED";
    /**
     * Unknown consent status, AdsConsent.requestInfoUpdate needs to be called to update it.
     */
    AdmobConsentStatus["UNKNOWN"] = "UNKNOWN";
})(AdmobConsentStatus || (AdmobConsentStatus = {}));
//# sourceMappingURL=consent-status.enum.js.map