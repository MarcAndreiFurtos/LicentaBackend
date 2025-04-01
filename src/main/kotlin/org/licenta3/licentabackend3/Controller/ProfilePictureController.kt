package org.licenta3.licentabackend3.Controller

import org.licenta3.licentabackend3.DTO.ProfilePictureDto
import org.licenta3.licentabackend3.Entities.ProfilePicture
import org.licenta3.licentabackend3.Service.ProfilePictureService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/profile-pictures")
class ProfilePictureController(private val profilePictureService: ProfilePictureService) {

    @PostMapping
    fun uploadProfilePicture(@RequestBody dto: ProfilePictureDto): ResponseEntity<ProfilePicture> {
        val savedProfilePicture = profilePictureService.saveProfilePicture(dto)
        return ResponseEntity.ok(savedProfilePicture)
    }

    @GetMapping("/{id}")
    fun getProfilePicture(@PathVariable id: Long): ResponseEntity<ProfilePicture> {
        val profilePicture = profilePictureService.getProfilePicture(id)
        return if (profilePicture != null) {
            ResponseEntity.ok(profilePicture)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PutMapping("/{id}")
    fun updateProfilePicture(
        @PathVariable id: Long,
        @RequestBody dto: ProfilePictureDto
    ): ResponseEntity<ProfilePicture> {
        val updatedProfilePicture = profilePictureService.updateProfilePicture(id, dto)
        return if (updatedProfilePicture != null) {
            ResponseEntity.ok(updatedProfilePicture)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/{id}")
    fun deleteProfilePicture(@PathVariable id: Long): ResponseEntity<Void> {
        profilePictureService.deleteProfilePicture(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping
    fun getAllProfilePictures(): ResponseEntity<List<ProfilePicture>> {
        val allPictures = profilePictureService.getAllProfilePictures()
        return ResponseEntity.ok(allPictures)
    }
}
