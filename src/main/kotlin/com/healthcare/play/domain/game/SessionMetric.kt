package com.healthcare.play.domain.game

import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.util.*

@Entity
@Table(
    name = "session_metric",
    indexes = [Index(columnList = "session_id")]
)
class SessionMetric(
    @Id @GeneratedValue @UuidGenerator
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    var session: GameSession,

    /** 지표 이름 (예: taps, correct, avgReactionMs) */
    @Column(name = "metric_key", nullable = false, length = 100)
    var key: String,

    /** 수치값 */
    @Column(name = "metric_value", nullable = false)
    var value: Double,

    /** 단위(선택) */
    @Column(length = 16)
    var unit: String? = null,

    /** 추가 데이터(JSON 문자열) */
    @Column(columnDefinition = "TEXT")
    var extra: String? = null
)