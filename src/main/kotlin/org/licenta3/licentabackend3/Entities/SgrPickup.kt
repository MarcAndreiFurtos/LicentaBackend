package org.licenta3.licentabackend3.Entities

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
data class SgrPickup(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "m_pickup")
    val mPickup: String,

    val destination: String,

    @Column(name = "eta")
    var estimatedTime: String? = null,

    @Column(name = "distance")
    var distance: String? = null, // Added field for distance

    @Enumerated(EnumType.STRING)
    var status: SgrPickupStatus = SgrPickupStatus.PENDING,

    val createdAt: LocalDateTime = LocalDateTime.now(),
    val value: Double = 0.0,
    var paidFor: Boolean = false
)

enum class SgrPickupStatus {
    PENDING, COMPLETED, CANCELLED
}
