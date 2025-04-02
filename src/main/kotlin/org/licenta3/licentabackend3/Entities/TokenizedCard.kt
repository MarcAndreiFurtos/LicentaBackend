package org.licenta3.licentabackend3.Entities

import jakarta.persistence.*
import java.security.MessageDigest

@Entity
data class TokenizedCard(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val token: String = "",
    val cardholderName: String = "",
    val hashedExpirationDate: String = "",

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    val user: User, // This reference links back to the user
) {
    companion object {
        fun hashExpirationDate(expirationDate: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
            return digest.digest(expirationDate.toByteArray()).joinToString("") { "%02x".format(it) }
        }
    }
}
