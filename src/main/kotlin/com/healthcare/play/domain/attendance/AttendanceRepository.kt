package com.healthcare.play.domain.attendance

import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate
import java.util.UUID

interface AttendanceRepository : JpaRepository<Attendance, UUID>{
    fun existsByUserIdAndDate(userId: UUID, date: LocalDate): Boolean
    fun findAllByUserIdAndDateBetweenOrderByDateAsc(userId: UUID, start: LocalDate, end:LocalDate): List<Attendance>
    fun findByUser_Id(userId: UUID): List<Attendance>
}