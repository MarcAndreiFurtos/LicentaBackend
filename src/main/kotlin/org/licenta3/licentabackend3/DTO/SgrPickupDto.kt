package org.licenta3.licentabackend3.DTO

data class SgrPickupDto(
    val driverLocation : String = "",
    val destination: String = "",
    val sackSizeLiters : Int = 0,
    val userId : Long = 0,
    val driverId : Long = 0,
    val cardId : Long = 0
)