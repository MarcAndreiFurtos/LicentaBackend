package org.licenta3.licentabackend3.DTO

data class ConfirmationLinkDto(
    val returnUrl: String,
    val refreshUrl: String,
)