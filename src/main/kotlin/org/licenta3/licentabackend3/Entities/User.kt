package org.licenta3.licentabackend3.Entities

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "app_user")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    var email: String,

    @Embedded
    var address: Address,

    var firstName: String = "",

    var lastName: String = "",

    @Column(length = 10000000)
    var profilePicture: String = "",

    var rating : Long = 0,

    var connectedAccount  :String = "",
)

@Embeddable
data class Address(
    val street: String,
    val city: String,
    val postalCode: String,
    val country: String
)
