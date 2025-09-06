package com.healthcare.play.web.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotEmpty
import java.util.*

data class ShapeStartRequest(
    @field:Min(1) val rows: Int = 2,
    @field:Min(1) val cols: Int = 2,
    @field:Min(1) val totalRounds: Int = 10,
    @field:NotEmpty val shapeSet: List<String> = listOf("CIRCLE","TRIANGLE","SQUARE","STAR"),
    val inputTimeoutMs: Int = 5000
)

data class ShapeStartResponse(
    val sessionId: UUID,
    val seed: Long,
    val rows: Int,
    val cols: Int,
    val totalRounds: Int,
    val shapeSet: List<String>,
    val roundIndex: Int,
    val target: String,
    val cells: List<String>,
    val inputTimeoutMs: Int
)

data class ShapeLayoutResponse(
    val roundIndex: Int,
    val target: String,
    val cells: List<String>
)

data class ShapeSubmitRequest(
    val roundIndex: Int,
    val chosenIndex: Int,
    val reactionMs: Int? = null
)

data class ShapeSubmitResponse(
    val roundIndex: Int,
    val correct: Boolean,
    val targetIndex: Int,
    val finished: Boolean,
    val nextRound: Int?
)

data class ShapeEndRequest(
    val totalRounds: Int,
    val hitCount: Int,
    val missCount: Int,
    val avgReactionMs: Int? = null
)
