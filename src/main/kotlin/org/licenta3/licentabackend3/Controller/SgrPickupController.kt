package org.licenta3.licentabackend3.Controller

import org.licenta3.licentabackend3.DTO.SgrPickupDto
import org.licenta3.licentabackend3.DTO.SgrPickupETAResponseDTO
import org.licenta3.licentabackend3.Entities.SgrPickup
import org.licenta3.licentabackend3.Service.SgrPickupService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/sgrpickup")
class SgrPickupController(private val sgrPickupService: SgrPickupService) {

    @GetMapping("/eta")
    fun getETA(
        @RequestParam mPickup: String,
        @RequestParam destination: String
    ): ResponseEntity<SgrPickupETAResponseDTO> {
        val etaResponse = sgrPickupService.calculateETA(mPickup, destination)
        return ResponseEntity.ok(etaResponse)
    }

    @PostMapping
    fun createSgrPickup(
        @RequestParam sgrDto: SgrPickupDto,
    ): ResponseEntity<SgrPickup> {
        val savedSgrPickup = sgrPickupService.saveSgrPickup(sgrDto.mPickup, sgrDto.destination, sgrDto.sackSizeLiters)
        return ResponseEntity.ok(savedSgrPickup)
    }

    @PutMapping("/{id}/complete")
    fun completeSgrPickup(@PathVariable id: Long): ResponseEntity<SgrPickup> {
        val updatedSgrPickup = sgrPickupService.completeSgrPickup(id)
        return ResponseEntity.ok(updatedSgrPickup)
    }

    @PutMapping("/{id}/cancel")
    fun cancelSgrPickup(@PathVariable id: Long): ResponseEntity<SgrPickup> {
        val updatedSgrPickup = sgrPickupService.cancelSgrPickup(id)
        return ResponseEntity.ok(updatedSgrPickup)
    }

    @GetMapping("/history")
    fun getCompletedSgrPickups(): ResponseEntity<List<SgrPickup>> {
        val completedPickups = sgrPickupService.getCompletedSgrPickups()
        return ResponseEntity.ok(completedPickups)
    }

    @GetMapping("/canceled")
    fun getCanceledSgrPickups(): ResponseEntity<List<SgrPickup>> {
        val canceledPickups = sgrPickupService.getCanceledSgrPickups()
        return ResponseEntity.ok(canceledPickups)
    }

    @PutMapping("/{id}/pay")
    fun markAsPaid(@PathVariable id: Long): ResponseEntity<SgrPickup> {
        val updatedSgrPickup = sgrPickupService.markAsPaid(id)
        return ResponseEntity.ok(updatedSgrPickup)
    }
}
