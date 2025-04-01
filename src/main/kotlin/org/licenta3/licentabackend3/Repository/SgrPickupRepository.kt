package org.licenta3.licentabackend3.Repository

import org.springframework.data.jpa.repository.JpaRepository
import org.licenta3.licentabackend3.Entities.SgrPickup
import org.licenta3.licentabackend3.Entities.SgrPickupStatus
import org.springframework.stereotype.Repository

@Repository
interface SgrPickupRepository : JpaRepository<SgrPickup, Long> {
    fun findByStatus(status: SgrPickupStatus): List<SgrPickup>
}
