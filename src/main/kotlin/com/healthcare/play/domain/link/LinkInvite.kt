package com.healthcare.play.domain.link

import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(name = "link_invite", indexes = [Index(columnList = "code", unique = true)])
class LinkInvite(
    @Id @GeneratedValue @UuidGenerator
    var id: UUID? = null,

    /** 1회용 초대 코드(사람이 읽을 수 있는 6~10자리) */
    @Column(nullable = false, unique = true, length = 16)
    var code: String,

    /** 초대한 사람(데이터 주인) */
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "inviter_id", nullable = false)
    var inviter: com.healthcare.play.domain.user.User,

    /** 수락 시 부여될 스코프들(스냅샷) */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "link_invite_scopes", joinColumns = [JoinColumn(name = "invite_id")])
    @Column(name = "scope") @Enumerated(EnumType.STRING)
    var scopes: MutableSet<LinkScope> = mutableSetOf(LinkScope.VIEW_REPORTS),

    @Column(nullable = false)
    var expiresAt: OffsetDateTime,

    var usedAt: OffsetDateTime? = null,

    @Column(nullable = false)
    var revoked: Boolean = false
)