package org.licenta3.licentabackend3.DTO

data class CardDto(
    val cardNumber: String,
    val cardholderName: String,
    val expirationDate: String,
    val userId: Long = 0
)
