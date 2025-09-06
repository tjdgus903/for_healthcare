package com.healthcare.play.domain.link

import org.springframework.data.jpa.repository.JpaRepository
import java.time.OffsetDateTime
import java.util.*

interface UserLinkRepository : JpaRepository<UserLink, UUID> {
    fun findAllByOwnerIdAndStatus(ownerId: UUID, status: LinkStatus): List<UserLink>
    fun findAllByGranteeIdAndStatus(granteeId: UUID, status: LinkStatus): List<UserLink>
    fun findByOwnerIdAndGranteeIdAndStatus(ownerId: UUID, granteeId: UUID, status: LinkStatus): List<UserLink>
}

interface LinkInviteRepository : JpaRepository<LinkInvite, UUID> {
    fun findByCode(code: String): Optional<LinkInvite>
    fun findAllByInviterIdAndRevokedFalseAndUsedAtIsNullAndExpiresAtAfter(inviterId: UUID, now: OffsetDateTime): List<LinkInvite>
}