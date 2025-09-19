package com.healthcare.play.domain.attendance

import com.healthcare.play.domain.user.User
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.annotations.UuidGenerator
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

enum class AttendanceStatus { PRESENT, ABSENT }

@Entity
@Table(
    name = "attendance",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "date"])],
    indexes = [
        Index(name = "ix_attendance_user_date", columnList = "user_id, date"),
        Index(name = "ix_attendance_date", columnList = "date")
    ]
)
class Attendance (
    @Id @GeneratedValue @UuidGenerator
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    var user: User,

    @Column(nullable = false)
    var date: LocalDate,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: AttendanceStatus = AttendanceStatus.PRESENT,

    @Column(nullable = false)
    var rewardClaimed: Boolean = false,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant? = null
)