package com.healthcare.play.domain.attendance

import com.healthcare.play.domain.user.User
import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.time.LocalDate
import java.util.UUID

enum class AttendanceStatus { PRESENT, ABSENT }

@Entity
@Table(
    name = "attendance",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "date"])]
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
    var rewardClaimed: Boolean = false
)