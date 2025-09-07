package com.healthcare.play.orgs

import org.springframework.stereotype.Component
import java.util.*

@Component
class OrgAcl(
    private val orgRepo: OrganizationRepository,
    private val memberRepo: OrganizationMemberRepository
) {
    fun requireMember(orgId: UUID, userId: UUID): Organization {
        val org = orgRepo.findById(orgId).orElseThrow()
        memberRepo.findByOrganization_IdAndUserId(orgId, userId).orElseThrow()
        return org
    }
    fun requireAdminOrManager(orgId: UUID, userId: UUID): Organization {
        val org = orgRepo.findById(orgId).orElseThrow()
        val m = memberRepo.findByOrganization_IdAndUserId(orgId, userId).orElseThrow()
        if (m.role != OrgRole.ADMIN && m.role != OrgRole.MANAGER) error("FORBIDDEN")
        return org
    }
    fun requireAdmin(orgId: UUID, userId: UUID): Organization {
        val org = orgRepo.findById(orgId).orElseThrow()
        val m = memberRepo.findByOrganization_IdAndUserId(orgId, userId).orElseThrow()
        if (m.role != OrgRole.ADMIN) error("FORBIDDEN")
        return org
    }
}