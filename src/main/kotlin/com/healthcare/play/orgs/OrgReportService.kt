package com.healthcare.play.orgs

import com.healthcare.play.domain.game.GameSessionRepository
import com.healthcare.play.domain.game.GameType
import com.healthcare.play.domain.game.SessionMetricRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

data class OrgSummaryRow(
    val gameId: UUID,
    val gameLabel: String,
    val sessions: Long,
    val avgScore: Double?,
    val avgAccuracy: Double?,
    val totalDurationSec: Long
)

data class OrgSummaryResponse(
    val orgId: UUID,
    val cohortId: UUID?,
    val from: Instant,
    val to: Instant,
    val rows: List<OrgSummaryRow>
)

@Service
class OrgReportService(
    private val acl: OrgAcl,
    private val cohortMemberRepo: CohortMemberRepository,
    private val sessionRepo: GameSessionRepository,
    private val metricRepo: SessionMetricRepository
) {
    @Transactional(readOnly = true)
    fun summaryByGame(orgId: UUID, actorUserId: UUID, from: Instant, to: Instant, cohortId: UUID?): OrgSummaryResponse {
        acl.requireMember(orgId, actorUserId)

        val userIds: Set<UUID> = if (cohortId != null) {
            cohortMemberRepo.findByCohort_Id(cohortId).map { it.userId }.toSet()
        } else {
            // 조직 전체 구성원(코호트 미지정 시) = 이 단순 버전에서는 모든 코호트 union
            cohortMemberRepo.findByCohort_IdIn( // 확장: 필요시 구현
                aclGetAllCohortIds(orgId)
            ).map { it.userId }.toSet()
        }

        if (userIds.isEmpty()) {
            return OrgSummaryResponse(orgId, cohortId, from, to, rows = emptyList())
        }

        // 세션/메트릭 집계 (필드명은 너의 엔티티에 맞춰 조정)
        val rows = sessionRepo.aggregateSummaryByGame(userIds, from, to)  // 커스텀 쿼리 필요
        return OrgSummaryResponse(orgId, cohortId, from, to, rows)
    }

    private fun aclGetAllCohortIds(orgId: UUID): List<UUID> {
        // 간단히: CohortRepository 통해 조회해도 됨. 서비스 분리 안하려고 내부에서 대체 구현
        return emptyList() // 필요하면 OrgService에서 주입받아 써도 OK
    }
}
