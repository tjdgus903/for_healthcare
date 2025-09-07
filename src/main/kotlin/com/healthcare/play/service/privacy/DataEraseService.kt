package com.healthcare.play.service.privacy

import com.healthcare.play.domain.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

@Service
class DataEraseService(private val userRepo: UserRepository) {

    @Transactional
    fun requestErase(userId: UUID) {
        val u = userRepo.findById(userId).orElseThrow()
        // 예: 소프트 삭제 플래그/시간
        u.deletedAt = Instant.now()      // User 엔티티에 컬럼 추가 필요 (nullable Instant)
        u.email = "deleted+${u.id}@example.invalid" // 재식별 최소화(선택)
        // 연관 데이터 비식별화/삭제는 배치로 분리해도 OK
    }
}