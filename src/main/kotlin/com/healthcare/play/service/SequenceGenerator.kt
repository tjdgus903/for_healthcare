package com.healthcare.play.service

import java.util.*

/** 시드 + 라운드 번호로 항상 동일한 패턴을 뽑아내는 순열 생성기 */
object SequenceGenerator {
    /** roundIndex(1부터) 길이의 패턴을 생성 */
    fun generate(seed: Long, roundIndex: Int, symbols: List<String>): List<String> {
        val r = Random(seed + roundIndex) // 라운드마다 결정적 시드
        return buildList(roundIndex) {
            repeat(roundIndex) {
                add(symbols[r.nextInt(symbols.size)])
            }
        }
    }
}