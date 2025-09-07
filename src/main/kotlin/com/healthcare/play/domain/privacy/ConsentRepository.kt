package com.healthcare.play.domain.privacy

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*
interface ConsentRepository : JpaRepository<Consent, UUID> {
    fun findTopByUser_IdAndDocOrderByAtDesc(userId: UUID, doc: String): Optional<Consent>
    fun findByUser_IdOrderByAtDesc(userId: UUID): List<Consent>
}