package com.healthcare.play.service.shape

import java.util.*


/** 시드 + 라운드 번호로 동일한 레이아웃을 생성 */
object ShapeLayoutGenerator {
    data class Layout(
        val rows: Int,
        val cols: Int,
        val target: String,       // 타깃 도형 id
        val cells: List<String>   // 셀별 도형 id (rows*cols 길이)
    )

    fun generate(seed: Long, roundIndex: Int, shapes: List<String>, rows: Int, cols: Int): Layout {
        val r = Random(seed + 1313L * roundIndex) // 라운드별 결정적 난수
        val total = rows * cols
        val target = shapes[r.nextInt(shapes.size)]
        val cells = MutableList(total) { shapes[r.nextInt(shapes.size)] }

        // 타깃이 1개 이상 등장하도록 보정(없으면 한 칸을 타깃으로 치환)
        if (!cells.contains(target)) {
            cells[r.nextInt(total)] = target
        }
        return Layout(rows, cols, target, cells)
    }
}