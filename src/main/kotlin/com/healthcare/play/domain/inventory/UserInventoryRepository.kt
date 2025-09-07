package com.healthcare.play.domain.inventory

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserInventoryRepository: JpaRepository<UserInventory, UUID> {
    fun findByUser_Id(userId: UUID): Optional<UserInventory>
}