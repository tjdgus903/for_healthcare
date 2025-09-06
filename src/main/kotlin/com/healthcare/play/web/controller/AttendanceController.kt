package org.example.com.healthcare.play.web.controller

import org.example.com.healthcare.play.domain.user.attendance.Attendance
import org.example.com.healthcare.play.service.AttendanceService
import org.example.com.healthcare.play.web.dto.CalendarDayResponse
import org.example.com.healthcare.play.web.dto.CheckAttendanceResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.YearMonth
import java.util.UUID

@RestController
@RequestMapping("/attendance")
class AttendanceController(
    private val attendanceService: AttendanceService
) {

    @PostMapping("/check")
    fun check(@RequestHeader("X-User-Id") userId: UUID): CheckAttendanceResponse{
        val r = attendanceService.check(userId)
        return CheckAttendanceResponse(r.date, r.isNewCheck, r.currentStreak)
    }

    @GetMapping("/calendar")
    fun calendar(
        @RequestHeader("X-User-Id") userId: UUID,
        @RequestParam("month") month: String
    ): List<CalendarDayResponse>{
        val ym = YearMonth.parse(month)
        return attendanceService.getCalendar(userId, ym).map { CalendarDayResponse(it.date, it.checked) }
    }
}