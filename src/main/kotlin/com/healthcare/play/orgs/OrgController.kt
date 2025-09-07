package com.healthcare.play.orgs

import com.healthcare.play.security.AuthPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

data class CreateOrgRes(val id: UUID)
data class MyOrgItem(val orgId: UUID, val orgName: String, val role: OrgRole)

@RestController
@RequestMapping("/orgs")
class OrgController(
    private val svc: OrgService,
    private val orgRepo: OrganizationRepository
) {
    @PostMapping
    fun createOrg(@AuthenticationPrincipal p: AuthPrincipal, @RequestBody req: CreateOrgReq): CreateOrgRes {
        val id = svc.createOrg(p.userId, req)
        return CreateOrgRes(id)
    }

    @GetMapping("/my")
    fun myOrgs(@AuthenticationPrincipal p: AuthPrincipal): List<MyOrgItem> {
        return svc.myOrgs(p.userId).map {
            val org = orgRepo.findById(it.organization.id!!).orElseThrow()
            MyOrgItem(org.id!!, org.name, it.role)
        }
    }

    @PostMapping("/{orgId}/members")
    fun addMember(@AuthenticationPrincipal p: AuthPrincipal,
                  @PathVariable orgId: UUID,
                  @RequestBody req: AddMemberReq): ResponseEntity<Void> {
        svc.addMember(orgId, p.userId, req)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/{orgId}/cohorts")
    fun createCohort(@AuthenticationPrincipal p: AuthPrincipal,
                     @PathVariable orgId: UUID,
                     @RequestBody req: CreateCohortReq): CreateOrgRes {
        val id = svc.createCohort(orgId, p.userId, req)
        return CreateOrgRes(id)
    }

    @GetMapping("/{orgId}/cohorts")
    fun cohorts(@AuthenticationPrincipal p: AuthPrincipal,
                @PathVariable orgId: UUID) = svc.cohorts(orgId, p.userId)

    @PostMapping("/{orgId}/cohorts/{cohortId}/members")
    fun addCohortMember(@AuthenticationPrincipal p: AuthPrincipal,
                        @PathVariable orgId: UUID,
                        @PathVariable cohortId: UUID,
                        @RequestBody req: AddCohortMemberReq): ResponseEntity<Void> {
        svc.addCohortMember(orgId, cohortId, p.userId, req)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/{orgId}/members/by-email")
    fun addMemberByEmail(
        @AuthenticationPrincipal p: AuthPrincipal,
        @PathVariable orgId: UUID,
        @RequestBody req: AddMemberByEmailReq
    ): ResponseEntity<Void> {
        svc.addMemberByEmail(orgId, p.userId, req)
        return ResponseEntity.ok().build()
    }
}