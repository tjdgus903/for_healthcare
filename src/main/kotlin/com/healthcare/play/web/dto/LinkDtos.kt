package com.healthcare.play.web.dto

import com.healthcare.play.domain.link.LinkScope
import java.time.OffsetDateTime
import java.util.UUID

data class CreateInviteRequest(
    val scopes: Set<LinkScope> = setOf(LinkScope.VIEW_REPORTS),
    val ttlMinutes: Long = 60
)
data class CreateInviteResponse(val code: String, val expiresAt: OffsetDateTime)

data class AcceptInviteRequest(val code: String)

data class LinkItem(
    val id: UUID,
    val ownerId: UUID,
    val granteeId: UUID,
    val scopes: Set<LinkScope>,
    val status: String,
    val createdAt: OffsetDateTime
)

data class UpdateScopesRequest(val scopes: Set<LinkScope>)