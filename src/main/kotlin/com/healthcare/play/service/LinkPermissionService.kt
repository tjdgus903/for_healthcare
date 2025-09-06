package com.healthcare.play.service

import com.healthcare.play.domain.link.LinkScope
import com.healthcare.play.domain.link.LinkStatus
import com.healthcare.play.domain.link.UserLinkRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class LinkPermissionService(
    private val linkRepo: UserLinkRepository
) {
    /**
     * caller가 owner 데이터에 대해 required 스코프들을 갖고 있는지 검사.
     * 본인은 항상 통과.
     */
    fun hasScopes(callerId: UUID, ownerId: UUID, required: Set<LinkScope>): Boolean {
        if (callerId == ownerId) return true
        val links = linkRepo.findByOwnerIdAndGranteeIdAndStatus(ownerId, callerId, LinkStatus.ACTIVE)
        val granted = links.flatMap { it.scopes }.toSet()
        return required.all { it in granted }
    }
}