package com.healthcare.play.orgs

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface OrganizationRepository : JpaRepository<Organization, UUID>
interface OrganizationMemberRepository : JpaRepository<OrganizationMember, UUID> {
    fun findByUserId(userId: UUID): List<OrganizationMember>
    fun findByOrganization_IdAndUserId(orgId: UUID, userId: UUID): Optional<OrganizationMember>
}
interface CohortRepository : JpaRepository<Cohort, UUID> {
    fun findByOrganization_Id(orgId: UUID): List<Cohort>
}
interface CohortMemberRepository : JpaRepository<CohortMember, UUID> {
    fun findByCohort_Id(cohortId: UUID): List<CohortMember>
    fun existsByCohort_IdAndUserId(cohortId: UUID, userId: UUID): Boolean
    fun findByCohort_IdIn(cohortIds: List<UUID>): List<CohortMember>

}
