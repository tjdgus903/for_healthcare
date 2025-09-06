package com.healthcare.play.domain.game

/** 게임 식별용 고정 enum (표시명은 DB의 name/description 사용) */
enum class GameType {
    COLOR_TAP,          // 빠른 색상 탭
    SEQUENCE_MEMORY,    // 순서 기억(패턴 재현)
    SHAPE_MATCH         // 도형 매칭
}