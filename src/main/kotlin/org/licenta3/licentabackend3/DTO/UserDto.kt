package org.licenta3.licentabackend3.DTO

data class UserDto(
    val email: String = "",
    val address: AddressDto  = AddressDto(),
    val profilePicture: String = "",
    val rating: Long = 0,
    val firstName: String = "",
    val lastName: String = "",
    val conectedAccount: String = "",
)
