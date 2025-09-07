package com.healthcare.play.domain.privacy

import com.healthcare.play.domain.user.User
import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity @Table(name = "consent",
    indexes=[Index(name="idx_consent_user", columnList = "user_id")])
class Consent(
    @Id @GeneratedValue var id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="user_id", nullable=false)
    var user: User,

    @Column(nullable=false) var doc: String,         // "terms", "privacy", "data_share"
    @Column(nullable=false) var version: String,     // "v1.0", ISO 날짜 등
    @Column(nullable=false) var accepted: Boolean,   // true=동의, false=철회
    @Column(nullable=false) var at: Instant = Instant.now(),
    @Column(nullable=true) var ip: String? = null,
    @Column(nullable=true) var userAgent: String? = null
)
