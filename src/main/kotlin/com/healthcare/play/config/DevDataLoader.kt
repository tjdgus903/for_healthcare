// src/main/kotlin/com/healthcare/play/config/DevDataLoader.kt
package com.healthcare.play.config

import com.healthcare.play.domain.user.User
import com.healthcare.play.domain.user.UserRepository
import com.healthcare.play.domain.user.UserRole
import com.healthcare.play.orgs.*
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*

@Component
@Profile("local")
class DevDataLoader(
    private val userRepo: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val orgRepo: OrganizationRepository,
    private val orgMemberRepo: OrganizationMemberRepository,
    private val cohortRepo: CohortRepository,
    private val cohortMemberRepo: CohortMemberRepository,
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun run(args: ApplicationArguments) {
        try {
            // 1) 사용자 보장 생성
            val u1 = userRepo.findByEmail("fhc").orElseGet {
                userRepo.save(
                    User(
                        email = "fhc",
                        passwordHash = passwordEncoder.encode("fhc"),
                        role = UserRole.PLAYER,
                    )
                ).also { log.info("▶ Dev user ensured: fhc/fhc (id={})", it.id) }
            }

            val u2 = userRepo.findByEmail("fhc2").orElseGet {
                userRepo.save(
                    User(
                        email = "fhc2",
                        passwordHash = passwordEncoder.encode("fhc2"),
                        role = UserRole.PLAYER,
                    )
                ).also { log.info("▶ Dev user ensured: fhc2/fhc2 (id={})", it.id) }
            }

            // 2) 조직 보장 생성
            val org = orgRepo.findAll().firstOrNull()
                ?: orgRepo.save(Organization(name = "Seoul Memory Clinic")).also {
                    log.info("▶ Dev org ensured: {}", it.name)
                }

            // 3) 조직 멤버 보장
            if (orgMemberRepo.findByOrganization_IdAndUserId(org.id!!, u1.id!!).isEmpty) {
                orgMemberRepo.save(
                    OrganizationMember(
                        organization = org,
                        userId = u1.id!!,
                        role = OrgRole.ADMIN,
                        joinedAt = Instant.now()
                    )
                )
            }
            if (orgMemberRepo.findByOrganization_IdAndUserId(org.id!!, u2.id!!).isEmpty) {
                orgMemberRepo.save(
                    OrganizationMember(
                        organization = org,
                        userId = u2.id!!,
                        role = OrgRole.VIEWER,
                        joinedAt = Instant.now()
                    )
                )
            }

            // 4) 코호트 보장 생성
            val cohort = cohortRepo.findByOrganization_Id(org.id!!).firstOrNull()
                ?: cohortRepo.save(Cohort(organization = org, name = "Group A")).also {
                    log.info("▶ Dev cohort ensured: {}", it.name)
                }

            // 5) 코호트 멤버 보장
            if (!cohortMemberRepo.existsByCohort_IdAndUserId(cohort.id!!, u1.id!!)) {
                cohortMemberRepo.save(CohortMember(cohort = cohort, userId = u1.id!!))
            }
            if (!cohortMemberRepo.existsByCohort_IdAndUserId(cohort.id!!, u2.id!!)) {
                cohortMemberRepo.save(CohortMember(cohort = cohort, userId = u2.id!!))
            }

        } catch (e: Exception) {
            // 개발 편의: 시드 실패해도 부팅은 계속 가도록(원하면 rethrow)
            log.warn("DevDataLoader skipped (non-fatal): {}", e.message)
        }
    }
}
