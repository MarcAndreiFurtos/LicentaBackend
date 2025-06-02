package org.licenta3.licentabackend3.DTO

data class CardDto(
    val cardNumber: String = "",
    val cardholderName: String = "",
    val accountId: String = "",
    val userId: Long = 0,
    val cardId: Long = 0

)
