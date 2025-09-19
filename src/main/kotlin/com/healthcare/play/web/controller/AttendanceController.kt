package com.healthcare.play.web.controller

import com.healthcare.play.security.AuthPrincipal
import com.healthcare.play.service.AttendanceService
import com.healthcare.play.web.dto.CalendarDayResponse
import com.healthcare.play.web.dto.CheckAttendanceResponse
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.YearMonth

@RestController
@RequestMapping("/attendance")
class AttendanceController(
    private val attendanceService: AttendanceService
) {
    @PostMapping("/check")
    fun check(@AuthenticationPrincipal principal: AuthPrincipal?): CheckAttendanceResponse {
        requireNotNull(principal) { "Unauthorized" }
        println(principal)
        val r = attendanceService.check(principal.userId)
        println(r)
        return CheckAttendanceResponse(r.date, r.isNewCheck, r.currentStreak)
    }

    @GetMapping("/calendar")
    fun calendar(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @RequestParam("month") month: String // YYYY-MM
    ): List<CalendarDayResponse> {
        val ym = YearMonth.parse(month)
        return attendanceService.getCalendar(principal.userId, ym)
            .map { CalendarDayResponse(it.date, it.checked) }
    }
}