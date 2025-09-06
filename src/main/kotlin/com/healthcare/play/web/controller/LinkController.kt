package com.healthcare.play.web.controller


import com.healthcare.play.domain.link.LinkScope
import com.healthcare.play.security.AuthPrincipal
import com.healthcare.play.service.LinkService
import com.healthcare.play.web.dto.*
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/links")
class LinkController(
    private val linkService: LinkService
) {
    /** 초대 생성: 내 데이터에 대한 권한을 상대에게 주기 위한 코드 발급 */
    @PostMapping("/invites")
    fun createInvite(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @RequestBody req: CreateInviteRequest
    ): ResponseEntity<CreateInviteResponse> {
        val invite = linkService.createInvite(
            inviterId = principal.userId,
            scopes = req.scopes.ifEmpty { setOf(LinkScope.VIEW_REPORTS) },
            ttlMinutes = req.ttlMinutes
        )
        return ResponseEntity.ok(CreateInviteResponse(invite.code, invite.expiresAt))
    }

    /** 초대 수락: 코드를 입력한 호출자에게 권한 부여 */
    @PostMapping("/accept")
    fun accept(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @RequestBody req: AcceptInviteRequest
    ): LinkItem {
        val link = linkService.acceptInvite(principal.userId, req.code)
        return LinkItem(
            id = link.id!!,
            ownerId = link.owner.id!!,
            granteeId = link.grantee.id!!,
            scopes = link.scopes,
            status = link.status.name,
            createdAt = link.createdAt
        )
    }

    /** 내가 권한을 준 목록(내가 owner) */
    @GetMapping("/outgoing")
    fun outgoing(@AuthenticationPrincipal principal: AuthPrincipal): List<LinkItem> =
        linkService.listOutgoing(principal.userId).map {
            LinkItem(it.id!!, it.owner.id!!, it.grantee.id!!, it.scopes, it.status.name, it.createdAt)
        }

    /** 내가 권한을 받은 목록(내가 grantee) */
    @GetMapping("/incoming")
    fun incoming(@AuthenticationPrincipal principal: AuthPrincipal): List<LinkItem> =
        linkService.listIncoming(principal.userId).map {
            LinkItem(it.id!!, it.owner.id!!, it.grantee.id!!, it.scopes, it.status.name, it.createdAt)
        }

    /** 링크 해지(오너만 가능) */
    @DeleteMapping("/{linkId}")
    fun revoke(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @PathVariable linkId: UUID
    ) {
        linkService.revokeLink(principal.userId, linkId)
    }

    /** 스코프 수정(오너만 가능) */
    @PatchMapping("/{linkId}/scopes")
    fun updateScopes(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @PathVariable linkId: UUID,
        @RequestBody req: UpdateScopesRequest
    ): LinkItem {
        val link = linkService.updateScopes(principal.userId, linkId, req.scopes)
        return LinkItem(link.id!!, link.owner.id!!, link.grantee.id!!, link.scopes, link.status.name, link.createdAt)
    }
}