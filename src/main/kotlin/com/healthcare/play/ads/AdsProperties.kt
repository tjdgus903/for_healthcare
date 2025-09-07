package com.healthcare.play.ads

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("ads")
data class AdsProperties (
    /** 전체 광고 기능 토글 */
    val enabled: Boolean = true,

    /** 세션 종료 후 1회 광고 허용 여부 */
    val allowPostSessionAd: Boolean = true,

    /** 보상형 광고 허용 여부 */
    val allowRewarded: Boolean = true,

    /** 동일 사용자 하루 최대 보상 수령 횟수 */
    val dailyRewardCap: Int = 5,

    /** 보상 간 최소 간격(초) - 떼탑/남용 방지 */
    val cooldownSeconds: Int = 60,

    /** 허용되는 보상 타입(힌트, 추가시간 등) */
    val allowedRewardTypes: List<String> = listOf("HINT", "EXTRA_TIME"),

    /** 개발용 우회(시그니처 검증 스킵 등) */
    val devMode: Boolean = true
)