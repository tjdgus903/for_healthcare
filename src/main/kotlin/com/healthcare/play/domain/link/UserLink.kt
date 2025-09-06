package com.healthcare.play.domain.link

import com.healthcare.play.domain.user.User
import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.time.OffsetDateTime
import java.util.UUID

enum class LinkStatus{ ACTIVE, REVOKED}

@Entity
@Table(
    name = "user_link",
    uniqueConstraints = [UniqueConstraint(columnNames = ["owner_id", "grantee_id", "status"])]
)
class UserLink(
    @Id @GeneratedValue @UuidGenerator
    var id: UUID? = null,

    /** 데이터 주인(whose reports are viewed) */
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "owner_id", nullable = false)
    var owner: User,

    /** 권한을 부여받은 사용자 */
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "grantee_id", nullable = false)
    var grantee: User,

    @Enumerated(EnumType.STRING)
    var status: LinkStatus = LinkStatus.ACTIVE,

    @Column(nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now(),

    /** 부여된 스코프들(H2/PG 호환 위해 별도 테이블) */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "user_link_scopes",
        joinColumns = [JoinColumn(name = "link_id")]
    )
    @Column(name = "scope")
    @Enumerated(EnumType.STRING)
    var scopes: MutableSet<LinkScope> = mutableSetOf(LinkScope.VIEW_REPORTS) // 기본값
)