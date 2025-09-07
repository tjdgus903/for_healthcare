package com.healthcare.play.service

import com.healthcare.play.domain.link.*
import com.healthcare.play.domain.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.SecureRandom
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.*

@Service
class LinkService(
    private val userRepo: UserRepository,
    private val inviteRepo: LinkInviteRepository,
    private val linkRepo: UserLinkRepository
) {
    private val zone = ZoneId.of("Asia/Seoul")
    private val random = SecureRandom()
    private val alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" // 혼동문자 제외

    private fun now(): OffsetDateTime = OffsetDateTime.now(zone)
    private fun genCode(len: Int = 8): String =
        buildString(len) { repeat(len) { append(alphabet[random.nextInt(alphabet.length)]) } }

    /** 초대 생성: 기존 유효 초대는 취소(정책) */
    @Transactional
    fun createInvite(inviterId: UUID, scopes: Set<LinkScope>, ttlMinutes: Long = 60): LinkInvite {
        val inviter = userRepo.findById(inviterId).orElseThrow()

        inviteRepo.findAllByInviterIdAndRevokedFalseAndUsedAtIsNullAndExpiresAtAfter(inviterId, now())
            .forEach { it.revoked = true }

        val invite = LinkInvite(
            code = genCode(8),
            inviter = inviter,
            scopes = scopes.toMutableSet(),
            expiresAt = now().plusMinutes(ttlMinutes)
        )
        return inviteRepo.save(invite)
    }

    /** 초대 수락 → 단방향 링크(owner=inviter → grantee=acceptee) 생성/업데이트 */
    @Transactional
    fun acceptInvite(accepteeId: UUID, code: String): UserLink {
        val invite = inviteRepo.findByCode(code).orElseThrow { IllegalArgumentException("초대 코드가 유효하지 않습니다.") }
        if (invite.revoked) error("초대가 취소되었습니다.")
        if (invite.usedAt != null) error("이미 사용된 코드입니다.")
        if (invite.expiresAt.isBefore(now())) error("코드가 만료되었습니다.")

        val owner = invite.inviter
        val grantee = userRepo.findById(accepteeId).orElseThrow()
        require(owner.id != grantee.id) { "자기 자신에게 권한을 줄 수 없습니다." }

        // 기존 ACTIVE 링크가 있으면 스코프 병합(중복 방지)
        val existing = linkRepo.findByOwnerIdAndGranteeIdAndStatus(owner.id!!, grantee.id!!, LinkStatus.ACTIVE).firstOrNull()
        val link = if (existing != null) {
            existing.scopes.addAll(invite.scopes)
            existing
        } else {
            UserLink(owner = owner, grantee = grantee, scopes = invite.scopes.toMutableSet())
        }
        val saved = linkRepo.save(link)
        invite.usedAt = now()
        return saved
    }

    @Transactional(readOnly = true)
    fun listOutgoing(inviterId: UUID): List<UserLink> =
        linkRepo.findAllByOwnerIdAndStatus(inviterId, LinkStatus.ACTIVE)

    @Transactional(readOnly = true)
    fun listIncoming(granteeId: UUID): List<UserLink> =
        linkRepo.findAllByGranteeIdAndStatus(granteeId, LinkStatus.ACTIVE)

    /** 링크 해지(OWNER 또는 MANAGE_LINK 권한자 정책에 따라 제한 가능) */
    @Transactional
    fun revokeLink(ownerId: UUID, linkId: UUID) {
        val link = linkRepo.findById(linkId).orElseThrow()
        require(link.owner.id == ownerId) { "해지 권한이 없습니다." }
        link.status = LinkStatus.REVOKED
        linkRepo.save(link)
    }

    /** 스코프 수정(추가/삭제) */
    @Transactional
    fun updateScopes(ownerId: UUID, linkId: UUID, scopes: Set<LinkScope>): UserLink {
        val link = linkRepo.findById(linkId).orElseThrow()
        require(link.owner.id == ownerId) { "수정 권한이 없습니다." }
        link.scopes = scopes.toMutableSet()
        return linkRepo.save(link)
    }
}