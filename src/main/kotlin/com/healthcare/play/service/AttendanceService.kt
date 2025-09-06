package org.example.com.healthcare.play.service

import org.example.com.healthcare.play.domain.user.UserRepository
import org.example.com.healthcare.play.domain.user.attendance.Attendance
import org.example.com.healthcare.play.domain.user.attendance.AttendanceRepository
import org.example.com.healthcare.play.domain.user.attendance.AttendanceStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.UUID

data class CheckResult(
    val date: LocalDate,
    val isNewCheck: Boolean,
    val currentStreak: Int,
)
data class CalendarDay(val date: LocalDate, val checked: Boolean)

@Service
class AttendanceService(
    private val attendanceRepository: AttendanceRepository,
    private val userRepository: UserRepository,
) {
    private val ZONE = ZoneId.of("Asia/Seoul")

   @Transactional
   fun check(userId: UUID): CheckResult {
       val today = LocalDate.now(ZONE)
       if(!userRepository.existsById(userId)) error("User not found")

       val existed = attendanceRepository.existsByUserIdAndDate(userId, today)
       if(!existed){
           val user = userRepository.findById(userId).orElseThrow()
           attendanceRepository.save(
               Attendance(user = user, date = today, status = AttendanceStatus.PRESENT)
           )
       }
       val streak = computeStreak(userId, today)
       return CheckResult(today, !existed, streak)
   }

    @Transactional(readOnly = true)
    fun getCalendar(userId: UUID, ym: YearMonth): List<CalendarDay> {
        val start = ym.atDay(1)
        val end = ym.atEndOfMonth()
        val map = attendanceRepository
            .findAllByUserIdAndDateBetweenOrderByDateAsc(userId, start, end)
            .associateBy { it.date }
        return (1..end.dayOfMonth).map { d ->
            val date = LocalDate.of(ym.year, ym.month, d)
            CalendarDay(date, map.containsKey(date))
        }
    }

    private fun computeStreak(userId: UUID, baseDate: LocalDate): Int {
        var d = baseDate
        var count = 0
        while (attendanceRepository.existsByUserIdAndDate(userId, d)) {
            count++; d = d.minusDays(1)
        }
        return count
    }
}