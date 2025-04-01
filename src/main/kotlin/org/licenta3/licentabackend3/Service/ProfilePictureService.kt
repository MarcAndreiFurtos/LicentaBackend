package org.licenta3.licentabackend3.Service

import org.licenta3.licentabackend3.DTO.ProfilePictureDto
import org.licenta3.licentabackend3.Entities.ProfilePicture
import org.licenta3.licentabackend3.Repository.ProfilePictureRepository
import org.springframework.stereotype.Service

@Service
class ProfilePictureService(
    private val profilePictureRepository: ProfilePictureRepository
) {

    fun saveProfilePicture(dto: ProfilePictureDto): ProfilePicture {
        val profilePicture = ProfilePicture(incriptedImmage = dto.incriptedImmage)
        return profilePictureRepository.save(profilePicture)
    }

    fun getProfilePicture(id: Long): ProfilePicture? {
        return profilePictureRepository.findById(id).orElse(null)
    }

    fun updateProfilePicture(id: Long, dto: ProfilePictureDto): ProfilePicture? {
        val existingProfilePicture = profilePictureRepository.findById(id).orElse(null)
        return if (existingProfilePicture != null) {
            existingProfilePicture.incriptedImmage = dto.incriptedImmage
            profilePictureRepository.save(existingProfilePicture)
        } else {
            null
        }
    }

    fun deleteProfilePicture(id: Long) {
        profilePictureRepository.deleteById(id)
    }

    fun getAllProfilePictures(): List<ProfilePicture> {
        return profilePictureRepository.findAll()
    }
}
