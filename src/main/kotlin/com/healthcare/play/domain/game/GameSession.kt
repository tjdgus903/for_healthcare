package com.healthcare.play.domain.game

import com.healthcare.play.domain.user.User
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.UuidGenerator
import org.hibernate.type.SqlTypes
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.*

@Entity
@Table(
    name = "game_session",
    indexes = [Index(columnList = "user_id"), Index(columnList = "game_id")]
)
class GameSession(
    @Id @GeneratedValue @UuidGenerator
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "game_id", nullable = false)
    var game: Game,

    @Column(name = "started_at", nullable = false)
    var startedAt: OffsetDateTime = OffsetDateTime.now(ZoneId.of("Asia/Seoul")),

    @Column(name = "ended_at")
    var endedAt: OffsetDateTime? = null,

    /** 클라이언트가 주거나, 서버가 end 시 자동 계산 */
    @Column(name = "duration_sec")
    var durationSec: Int? = null,   // DB가 int4면 Int로 유지

    /** 총점(게임별 규칙 반영한 점수) */
    var score: Int? = null,

    /** 정확도(0.0~1.0) */
    var accuracy: Double? = null,

    /** 세션 요약/설정/난이도 등 JSON */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "game_meta", columnDefinition = "jsonb", nullable = false)
    var meta: String? = null
)
