package com.healthcare.play.domain.game

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class GameType(
    /** 외부로 노출되는 안정적 코드 (소문자-케밥 권장) */
    val code: String,
    val displayName: String,
) {
    COLOR_TAP("color-tap", "Color Tap"),
    SEQUENCE_MEMORY("sequence-memory", "Sequence Memory"),
    SHAPE_MATCH("shape-match", "Shape Match");

    /** JSON 직렬화 시 이 값을 쓰고 싶다면 활성화 */
    @JsonValue
    fun toJson(): String = code

    companion object {
        /** JSON 역직렬화/문자매핑 시 사용 */
        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        @JvmStatic
        fun fromJsonOrName(s: String): GameType =
            entries.firstOrNull {
                it.code.equals(s, true) || it.name.equals(s, true)
            } ?: throw IllegalArgumentException("Unknown game type: $s")

        /** 컨트롤러/서비스에서 직접 호출할 헬퍼 */
        fun fromCode(s: String): GameType = fromJsonOrName(s)
    }
}