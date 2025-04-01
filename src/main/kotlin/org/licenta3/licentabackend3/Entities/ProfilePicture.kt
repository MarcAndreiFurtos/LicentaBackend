package org.licenta3.licentabackend3.Entities

import jakarta.persistence.*

@Entity
data class ProfilePicture(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id : Long = 0,
    @Column(length = 10000000)
    var incriptedImmage: String  = "",
)