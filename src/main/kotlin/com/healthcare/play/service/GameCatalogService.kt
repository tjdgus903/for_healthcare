package com.healthcare.play.service

import com.healthcare.play.domain.game.Game
import com.healthcare.play.domain.game.GameRepository
import com.healthcare.play.domain.game.GameType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GameCatalogService (
    private val gameRepo: GameRepository,
){
    fun listActive(): List<Game> = gameRepo.findAllByActiveTrueOrderByNameAsc()
    fun getByType(type: GameType): Game =
        gameRepo.findByType(type) ?: throw IllegalArgumentException("Game not found: $type")

    @Transactional
    fun ensureDefaults(){
        fun upsert(type: GameType, name: String, desc: String){
            var existing = gameRepo.findByType(type)
            if(existing == null){
                gameRepo.save(Game(type = type, name = name, description = desc, active = true))
            }else{
                existing.name = name
                existing.description = desc
                existing.active = true
                gameRepo.save(existing)
            }
        }
        upsert(GameType.COLOR_TAP, "Color Tap", "색을 보고 즉시 탭하여 반응속도를 훈련합니다.")
        upsert(GameType.SEQUENCE_MEMORY, "Sequence Memory", "표시된 순서를 기억해 재현합니다.")
        upsert(GameType.SHAPE_MATCH, "Shape Match", "도형을 빠르고 정확하게 매칭합니다.")
    }
}