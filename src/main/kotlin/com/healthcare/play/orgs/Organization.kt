package com.healthcare.play.orgs

import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "organization")
class Organization(
    @Id @GeneratedValue var id: UUID? = null,
    @Column(nullable = false, unique = true) var name: String,
    @Column(nullable = false) var createdAt: Instant = Instant.now()
)

enum class OrgRole { ADMIN, MANAGER, VIEWER }

@Entity
@Table(
    name = "organization_member",
    uniqueConstraints = [UniqueConstraint(columnNames = ["org_id", "user_id"])],
    indexes = [Index(name="idx_org_member_org", columnList = "org_id"), Index(name="idx_org_member_user", columnList="user_id")]
)
class OrganizationMember(
    @Id @GeneratedValue var id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "org_id", nullable = false)
    var organization: Organization,

    @Column(name="user_id", nullable = false) var userId: UUID,
    @Enumerated(EnumType.STRING) @Column(nullable = false) var role: OrgRole = OrgRole.VIEWER,
    @Column(nullable = false) var joinedAt: Instant = Instant.now()
)

@Entity
@Table(
    name = "cohort",
    uniqueConstraints = [UniqueConstraint(columnNames = ["org_id", "name"])],
    indexes = [Index(name="idx_cohort_org", columnList = "org_id")]
)
class Cohort(
    @Id @GeneratedValue var id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="org_id", nullable=false)
    var organization: Organization,
    @Column(nullable = false) var name: String,
    @Column(nullable = false) var createdAt: Instant = Instant.now()
)

@Entity
@Table(
    name = "cohort_member",
    uniqueConstraints = [UniqueConstraint(columnNames = ["cohort_id", "user_id"])],
    indexes = [Index(name="idx_cohort_member_cohort", columnList = "cohort_id"), Index(name="idx_cohort_member_user", columnList="user_id")]
)
class CohortMember(
    @Id @GeneratedValue var id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="cohort_id", nullable=false)
    var cohort: Cohort,
    @Column(name="user_id", nullable=false) var userId: UUID,
    @Column(nullable=false) var joinedAt: Instant = Instant.now()
)
