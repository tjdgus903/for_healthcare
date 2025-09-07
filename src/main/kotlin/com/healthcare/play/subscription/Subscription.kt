package com.healthcare.play.subscription

import com.healthcare.play.domain.user.User
import jakarta.persistence.*
import java.time.Instant
import java.util.*

enum class Platform { ANDROID, IOS }
enum class SubStatus { ACTIVE, CANCELED, EXPIRED }

@Entity
@Table(
    name = "subscription",
    indexes = [
        Index(name = "idx_sub_user", columnList = "user_id"),
        Index(name = "idx_sub_token", columnList = "purchaseToken")
    ]
)
class Subscription(
    @Id @GeneratedValue
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var platform: Platform,

    @Column(nullable = false)
    var productId: String,               // ex) sub_premium_monthly

    @Column(nullable = false, unique = true)
    var purchaseToken: String,           // Play: purchaseToken / iOS: transactionId 등

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: SubStatus = SubStatus.ACTIVE,

    @Column(nullable = false)
    var startedAt: Instant = Instant.now(),

    @Column(nullable = true)
    var expiresAt: Instant? = null,

    @Column(nullable = true)
    var canceledAt: Instant? = null,

    @Column(nullable = true)
    var lastVerifiedAt: Instant? = null,

    @Lob
    @Column(nullable = true)
    var rawPayload: String? = null      // 검증 응답 전체 저장(옵션)
)