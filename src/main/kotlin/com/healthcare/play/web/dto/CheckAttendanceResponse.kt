package org.example.com.healthcare.play.web.dto

import java.time.LocalDate

class CheckAttendanceResponse(
    val date: LocalDate,
    val isNewCheck: Boolean,
    val currentStreak: Int,
)

data class CalendarDayResponse(
    val date: LocalDate,
    val checked: Boolean
)