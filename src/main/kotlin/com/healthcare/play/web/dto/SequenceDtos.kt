package com.healthcare.play.web.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotEmpty

data class SequenceStartRequest(
    @field:Min(3) val totalRounds: Int = 8,
    @field:NotEmpty val symbolSet: List<String> = listOf("R","G","B","Y"),
    val showMs: Int = 800,
    val inputTimeoutMs: Int = 6000
)

data class SequenceStartResponse(
    val sessionId: java.util.UUID,
    val seed: Long,
    val totalRounds: Int,
    val symbolSet: List<String>,
    val roundIndex: Int,
    val pattern: List<String>,
    val showMs: Int,
    val inputTimeoutMs: Int
)

data class SequenceGetPatternResponse(
    val roundIndex: Int,
    val pattern: List<String>
)

data class SequenceSubmitRequest(
    val roundIndex: Int,
    val attempt: List<String>,
    val reactionMs: Int? = null
)

data class SequenceSubmitResponse(
    val roundIndex: Int,
    val correct: Boolean,
    val expected: List<String>,
    val finished: Boolean,
    val nextRound: Int?
)

data class SequenceEndRequest(
    val maxLevel: Int,
    val totalRounds: Int,
    val successCount: Int,
    val failCount: Int,
    val avgInputMs: Int? = null
)