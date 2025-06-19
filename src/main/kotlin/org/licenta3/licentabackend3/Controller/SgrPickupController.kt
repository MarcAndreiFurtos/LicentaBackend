package org.licenta3.licentabackend3.Controller

import org.licenta3.licentabackend3.DTO.SgrPickupDto
import org.licenta3.licentabackend3.DTO.SgrPickupETAResponseDTO
import org.licenta3.licentabackend3.Entities.SgrPickup
import org.licenta3.licentabackend3.Entities.SgrPickupStatus
import org.licenta3.licentabackend3.Repository.SgrPickupRepository
import org.licenta3.licentabackend3.Service.SgrPickupService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/sgrPickup")
class SgrPickupController(
    private val sgrPickupService: SgrPickupService,
    private val sgrPickupRepository: SgrPickupRepository
) {

    @GetMapping("/{id}/eta")
    fun getETA(
        @PathVariable id: Long
    ): ResponseEntity<SgrPickupETAResponseDTO> {
        val etaResponse = sgrPickupService.calculateETA(id)
        return ResponseEntity.ok(etaResponse)
    }

    @PostMapping
    fun createSgrPickup(
        @RequestBody sgrDto: SgrPickupDto,
    ): ResponseEntity<SgrPickup> {
        val savedSgrPickup = sgrPickupService.saveSgrPickup(sgrDto.driverLocation, sgrDto.pickupLocation, sgrDto.sackSizeLiters, sgrDto.userId)
        return ResponseEntity.ok(savedSgrPickup)
    }

    @PutMapping("/{id}/complete")
    fun completeSgrPickup(@PathVariable id: Long,@RequestBody sgrDto: SgrPickupDto): ResponseEntity<SgrPickup> {
        val updatedSgrPickup = sgrPickupService.completeSgrPickup(id,sgrDto)
        return ResponseEntity.ok(updatedSgrPickup)
    }

    @PutMapping("/{id}/cancel")
    fun cancelSgrPickup(@PathVariable id: Long): ResponseEntity<SgrPickup> {
        val updatedSgrPickup = sgrPickupService.cancelSgrPickup(id)
        return ResponseEntity.ok(updatedSgrPickup)
    }

    @GetMapping("/{userId}/history")
    fun getCompletedSgrPickups(@PathVariable userId: Long): ResponseEntity<List<SgrPickup>> {
        val completedPickups = sgrPickupService.getCompletedSgrPickups(userId)
        return ResponseEntity.ok(completedPickups)
    }

    @GetMapping("/{userId}/historyDriver")
    fun getCompletedSgrPickupsDriver(@PathVariable userId: Long): ResponseEntity<List<SgrPickup>> {
        val completedPickups = sgrPickupService.getCompletedSgrPickupsDriver(userId)
        return ResponseEntity.ok(completedPickups)
    }
    @GetMapping("/{userId}/pending")
    fun getPendingSgrPickups(@PathVariable userId: Long): ResponseEntity<List<SgrPickup>> {
        val pendingPickups = sgrPickupService.getPendingSgrPickups(userId)
        return ResponseEntity.ok(pendingPickups)
    }

    @GetMapping("/pending")
    fun getPendingSgrPickupsWithoutId(): ResponseEntity<List<SgrPickup>> {
        val pendingPickups = sgrPickupService.getPendingSgrPickupsWithoutId()
        return ResponseEntity.ok(pendingPickups)
    }

    @GetMapping("/{userId}/canceled")
    fun getCanceledSgrPickups(@PathVariable userId: Long): ResponseEntity<List<SgrPickup>> {
        val canceledPickups = sgrPickupService.getCanceledSgrPickups(userId)
        return ResponseEntity.ok(canceledPickups)
    }
    @GetMapping("/{id}/status")
    fun getPickupStatus(@PathVariable id: Long): ResponseEntity<SgrPickupStatus> {
        return ResponseEntity.ok(sgrPickupService.getPickupStatus(id))
    }
    @PutMapping("/{id}/pay")
    fun markAsPaid(@PathVariable id: Long, @RequestBody sgrDto: SgrPickupDto): ResponseEntity<SgrPickup> {
        val updatedSgrPickup = sgrPickupService.markAsPaid(id, sgrDto)
        return ResponseEntity.ok(updatedSgrPickup)
    }

    @PutMapping("/{id}/progress")
    fun markInProgress(@PathVariable id: Long,  @RequestBody sgrDto: SgrPickupDto): ResponseEntity<SgrPickup> {
        val updatedSgrPickup = sgrPickupService.markInProgress(id, sgrDto)
        return ResponseEntity.ok(updatedSgrPickup)
    }

    @PutMapping("/{id}/dLocation")
    fun updateDriverLocation(@PathVariable id: Long, @RequestBody sgrDto: SgrPickupDto): ResponseEntity<SgrPickup> {
        val updatedSgrPickup = sgrPickupService.updateDriverLocation(id, sgrDto)
        return ResponseEntity.ok(updatedSgrPickup)
    }

    @GetMapping("/{id}")
    fun getPickupById(@PathVariable id: Long): ResponseEntity<SgrPickup> {
        val sgrPickup = sgrPickupRepository.findById(id).orElseThrow()
        return ResponseEntity.ok(sgrPickup)
    }
}
