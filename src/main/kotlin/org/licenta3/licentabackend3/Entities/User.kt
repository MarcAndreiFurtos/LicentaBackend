package org.licenta3.licentabackend3.Entities

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    var email: String,
    var password: String,

    @Embedded
    var address: Address,

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    var tokenizedCards: List<TokenizedCard> = mutableListOf(),

    @OneToOne(cascade = [CascadeType.ALL])
    var profilePicture: ProfilePicture? = null
)

@Embeddable
data class Address(
    val street: String,
    val city: String,
    val postalCode: String,
    val country: String
)
