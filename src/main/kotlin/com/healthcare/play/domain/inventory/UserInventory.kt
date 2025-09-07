package com.healthcare.play.domain.inventory

import com.healthcare.play.domain.user.User
import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.util.*

@Entity
@Table(name = "user_inventory")
class UserInventory(
    @Id @GeneratedValue @UuidGenerator
    var id: UUID? = null,

    @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name="user_id", nullable=false, unique=true)
    var user: User,

    /** 힌트 토큰 수(보상형 광고로 적립) */
    @Column(name="hint_tokens", nullable=false)
    var hintTokens: Int = 0,

    /** 추가 시간 토큰 수 */
    @Column(name="extra_time_tokens", nullable=false)
    var extraTimeTokens: Int = 0
)