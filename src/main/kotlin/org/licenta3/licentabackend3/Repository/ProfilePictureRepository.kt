package org.licenta3.licentabackend3.Repository

import org.licenta3.licentabackend3.Entities.ProfilePicture
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProfilePictureRepository : JpaRepository<ProfilePicture, Long>
