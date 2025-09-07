package com.healthcare.play.domain.reward

import com.healthcare.play.domain.user.User
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UuidGenerator
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(name = "reward", indexes = [Index(columnList = "user_id, created_at")])
class Reward(
    @Id @GeneratedValue @UuidGenerator
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="user_id", nullable=false)
    var user: User,

    /** 보상 타입(HINT / EXTRA_TIME 등) */
    @Column(name="reward_type", nullable=false, length=32)
    var rewardType: String,

    /** 보상 값(개수/초 등) */
    @Column(name="amount", nullable=false)
    var amount: Int = 1,

    /** 광고 네트워크 식별자(예: admob) */
    @Column(name="ad_network", length=32)
    var adNetwork: String? = null,

    /** 광고 이벤트(콜백) 고유 식별자(중복 방지용) */
    @Column(name="ad_event_id", length=128, unique=true)
    var adEventId: String? = null,

    /** 어떤 세션 후 보상인지(선택) */
    @Column(name="session_id")
    var sessionId: UUID? = null,

    @CreationTimestamp
    @Column(name="created_at", nullable=false)
    var createdAt: OffsetDateTime? = null
)