package com.healthcare.play.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.healthcare.play.domain.game.*
import com.healthcare.play.domain.user.UserRepository
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.hibernate.query.sqm.tree.SqmNode.log
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.*

@Service
class GameSessionService(
    private val userRepo: UserRepository,
    private val gameRepo: GameRepository,
    private val sessionRepo: GameSessionRepository,
    private val metricRepo: SessionMetricRepository,
) {
    companion object {
        private val log = LoggerFactory.getLogger(GameSessionService::class.java)
    }

    @PersistenceContext
    private lateinit var em: EntityManager   // ← entityManager 주입

    private val zone = ZoneId.of("Asia/Seoul")
    private val M = jacksonObjectMapper()

    @Transactional
    fun start(userId: UUID, type: GameType, meta: Map<String, Any>? = null): GameSession {
        // 앱이 "현재" 보고 있는 스키마/서치패스/테이블카운트 확인 (진단용)
        @Suppress("UNCHECKED_CAST")
        val row = em.createNativeQuery(
            "select current_schema(), current_setting('search_path'), (select count(*) from game)"
        ).singleResult as Array<*>
        log.info("schema={}, search_path={}, game_count={}", row[0], row[1], row[2])

        val who = em.createNativeQuery("select current_user, session_user").singleResult
        log.info("user={}", who)

        log.info("userId={}", userId)
        log.info("requestedType={}", type.name)  // ← GameType(타입) 말고, type(값)을 찍기

        print("1. ")
        println(userId)
        print("2. ")
        println(GameType)
        val user = userRepo.findById(userId).orElseThrow()
        print("3. ")
        println(user)
        val game = gameRepo.findByType(type) ?: throw IllegalArgumentException("Game not found: $type")
        //val game = gameRepo.findByType(type)
        print("4. ")
        println(game)
        val session = GameSession(
            user = user,
            game = game,
            startedAt = OffsetDateTime.now(zone),
            meta = meta?.let { M.writeValueAsString(it) }
        )
        print("5. ")
        println(session)
        return sessionRepo.save(session)
    }

    /** 지표 추가(업서트 아님: 누적 기록을 위해 여러 번 호출 가능) */
    @Transactional
    fun addMetrics(sessionId: UUID, items: List<MetricItem>) {
        val session = sessionRepo.findById(sessionId).orElseThrow()
        items.forEach {
            metricRepo.save(
                SessionMetric(
                    session = session,
                    key = it.key,
                    value = it.value,
                    unit = it.unit,
                    extra = it.extra?.let { j -> M.writeValueAsString(j) }
                )
            )
        }
    }

    /** 세션 종료(점수/정확도/소요시간 계산) */
    @Transactional
    fun end(sessionId: UUID, score: Int?, accuracy: Double?, durationSec: Int?, meta: Map<String, Any>?): GameSession {
        val s = sessionRepo.findById(sessionId).orElseThrow()
        val endTime = OffsetDateTime.now(zone)
        s.endedAt = endTime
        if (durationSec == null && s.startedAt != null) {
            s.durationSec = Duration.between(s.startedAt, endTime).seconds.toInt()
        } else {
            s.durationSec = durationSec
        }
        s.score = score
        s.accuracy = accuracy
        if (meta != null) {
            s.meta = jacksonObjectMapper().writeValueAsString(meta)
        }
        return sessionRepo.save(s)
    }

    @Transactional(readOnly = true)
    fun listMySessions(userId: UUID, type: GameType?): List<GameSession> =
        if (type == null) sessionRepo.findAllByUserIdOrderByStartedAtDesc(userId)
        else sessionRepo.findAllByUser_IdAndGame_TypeOrderByStartedAtDesc(userId, type)

}

/** 클라이언트가 보내는 지표 포맷 */
data class MetricItem(
    val key: String,
    val value: Double,
    val unit: String? = null,
    val extra: Map<String, Any>? = null
)