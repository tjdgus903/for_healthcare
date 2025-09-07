package com.healthcare.play.service.privacy

import com.fasterxml.jackson.databind.ObjectMapper
import com.healthcare.play.domain.attendance.AttendanceRepository
import com.healthcare.play.domain.game.GameSessionRepository
import com.healthcare.play.subscription.SubscriptionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.ByteArrayOutputStream
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Service
class DataExportService(
    private val attendanceRepo: AttendanceRepository,
    private val sessionRepo: GameSessionRepository,
    private val subRepo: SubscriptionRepository,
    private val om: ObjectMapper
) {
    @Transactional(readOnly = true)
    fun exportUser(userId: UUID): ByteArray {
        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { zip ->
            fun put(name: String, value: Any) {
                zip.putNextEntry(ZipEntry("$name.json"))
                zip.write(om.writerWithDefaultPrettyPrinter().writeValueAsBytes(value))
                zip.closeEntry()
            }
            put("attendance", attendanceRepo.findByUser_Id(userId))
            put("sessions", sessionRepo.findAllByUser_IdOrderByStartedAtDesc(userId))
            put("subscriptions", subRepo.findByUser_Id(userId))
        }
        return out.toByteArray()
    }
}