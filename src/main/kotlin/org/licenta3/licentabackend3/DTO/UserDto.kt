package org.licenta3.licentabackend3.DTO

data class UserDto(
    val email: String,
    val password: String,
    val address: AddressDto,
    val profilePicture: ProfilePictureDto? = null,
    val rating: Long = 0
)
