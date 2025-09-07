package com.healthcare.play.orgs

import com.healthcare.play.domain.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

data class CreateOrgReq(val name: String)
data class AddMemberReq(val userId: UUID, val role: OrgRole = OrgRole.VIEWER)
data class CreateCohortReq(val name: String)
data class AddCohortMemberReq(val userId: UUID)
data class AddMemberByEmailReq(val email: String, val role: OrgRole = OrgRole.VIEWER)

@Service
class OrgService(
    private val orgRepo: OrganizationRepository,
    private val memberRepo: OrganizationMemberRepository,
    private val cohortRepo: CohortRepository,
    private val cohortMemberRepo: CohortMemberRepository,
    private val acl: OrgAcl,
    private val userRepo: UserRepository
) {
    @Transactional
    fun createOrg(ownerUserId: UUID, req: CreateOrgReq): UUID {
        val org = orgRepo.save(Organization(name = req.name))
        memberRepo.save(OrganizationMember(organization = org, userId = ownerUserId, role = OrgRole.ADMIN))
        return org.id!!
    }

    @Transactional(readOnly = true)
    fun myOrgs(userId: UUID) = memberRepo.findByUserId(userId)

    @Transactional
    fun addMember(orgId: UUID, actorUserId: UUID, req: AddMemberReq) {
        val org = acl.requireAdmin(orgId, actorUserId)
        if (memberRepo.findByOrganization_IdAndUserId(org.id!!, req.userId).isEmpty)
            memberRepo.save(OrganizationMember(organization = org, userId = req.userId, role = req.role))
    }

    @Transactional
    fun createCohort(orgId: UUID, actorUserId: UUID, req: CreateCohortReq): UUID {
        val org = acl.requireAdminOrManager(orgId, actorUserId)
        val cohort = cohortRepo.save(Cohort(organization = org, name = req.name))
        return cohort.id!!
    }

    @Transactional
    fun addCohortMember(orgId: UUID, cohortId: UUID, actorUserId: UUID, req: AddCohortMemberReq) {
        acl.requireAdminOrManager(orgId, actorUserId)
        val cohort = cohortRepo.findById(cohortId).orElseThrow()
        if (!cohort.organization.id!!.equals(orgId)) error("COHORT_NOT_IN_ORG")
        if (!cohortMemberRepo.existsByCohort_IdAndUserId(cohortId, req.userId))
            cohortMemberRepo.save(CohortMember(cohort = cohort, userId = req.userId))
    }

    @Transactional(readOnly = true)
    fun cohorts(orgId: UUID, actorUserId: UUID) : List<Cohort> {
        acl.requireMember(orgId, actorUserId)
        return cohortRepo.findByOrganization_Id(orgId)
    }

    @Transactional
    fun addMemberByEmail(orgId: UUID, actorUserId: UUID, req: AddMemberByEmailReq) {
        val org = acl.requireAdmin(orgId, actorUserId)
        val user = userRepo.findByEmail(req.email).orElseThrow()   // 존재하는 유저 전제
        if (memberRepo.findByOrganization_IdAndUserId(org.id!!, user.id!!).isEmpty) {
            memberRepo.save(OrganizationMember(organization = org, userId = user.id!!, role = req.role))
        }
    }
}