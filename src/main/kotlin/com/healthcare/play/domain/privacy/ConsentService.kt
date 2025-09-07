package com.healthcare.play.service.privacy

import com.healthcare.play.domain.privacy.Consent
import com.healthcare.play.domain.privacy.ConsentRepository
import com.healthcare.play.domain.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

data class ConsentUpsertReq(val doc: String, val version: String, val accepted: Boolean)

@Service
class ConsentService(
    private val userRepo: UserRepository,
    private val consentRepo: ConsentRepository
) {
    @Transactional
    fun upsert(userId: UUID, req: ConsentUpsertReq, ip: String?, ua: String?) {
        val user = userRepo.findById(userId).orElseThrow()
        consentRepo.save(
            Consent(user=user, doc=req.doc, version=req.version, accepted=req.accepted,
                at=Instant.now(), ip=ip, userAgent=ua)
        )
    }

    @Transactional(readOnly = true)
    fun history(userId: UUID) = consentRepo.findByUser_IdOrderByAtDesc(userId)
}
