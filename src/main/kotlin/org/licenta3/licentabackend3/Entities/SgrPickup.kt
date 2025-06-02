package org.licenta3.licentabackend3.Entities

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
data class SgrPickup(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "d_location")
    var driverLocation: String,

    val destination: String,

    @Column(name = "eta")
    var estimatedTime: String? = null,

    @Column(name = "distance")
    var distance: String? = null,

    @Enumerated(EnumType.STRING)
    var status: SgrPickupStatus = SgrPickupStatus.PENDING,

    val createdAt: LocalDateTime = LocalDateTime.now(),
    val value: Double = 0.0,
    var paidFor: Boolean = false,
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne
    @JoinColumn(name = "driver_id", nullable = false)
    var driver: User ,
)

enum class SgrPickupStatus {
    PENDING, IN_PROGRESS, COMPLETED, CANCELLED
}
