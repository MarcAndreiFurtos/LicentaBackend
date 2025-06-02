package org.licenta3.licentabackend3.Service

import jakarta.transaction.Transactional
import org.licenta3.licentabackend3.DTO.UserDto
import org.licenta3.licentabackend3.Entities.Address
import org.licenta3.licentabackend3.Entities.TokenizedCard
import org.licenta3.licentabackend3.Entities.User
import org.licenta3.licentabackend3.Repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(private val userRepository: UserRepository) {

    fun createUser(userDto: UserDto): User {
        val address = Address(
            street = userDto.address.street,
            city = userDto.address.city,
            postalCode = userDto.address.postalCode,
            country = userDto.address.country
        )


        val user = User(
            email = userDto.email,
            address = address,
            profilePicture = userDto.profilePicture,
            rating = userDto.rating,
            firstName = userDto.firstName,
            lastName = userDto.lastName,
            connectedAccount = userDto.conectedAccount
        )

        return userRepository.save(user)
    }

    fun getUserById(id: Long): User {
        return userRepository.findById(id).orElseThrow {
            RuntimeException("User not found with ID: $id")
        }
    }

    fun getUserByEmail(email: String): User {
        return userRepository.findByEmail(email)[0];
    }

    @Transactional
    fun updateUser(id: Long, userDto: UserDto): User {
        val existingUser = getUserById(id)
        existingUser.email = userDto.email
        existingUser.address = Address(
            street = userDto.address.street,
            city = userDto.address.city,
            postalCode = userDto.address.postalCode,
            country = userDto.address.country
        )
        existingUser.profilePicture = userDto.profilePicture
        existingUser.rating = userDto.rating
        return userRepository.save(existingUser)
    }

    @Transactional
    fun updateUserProfilePicture(id: Long, profilePicture: String): User {
        val existingUser = getUserById(id)
        existingUser.profilePicture = profilePicture
        return userRepository.save(existingUser)
    }

    fun deleteUser(id: Long) {
        userRepository.deleteById(id)
    }

    fun getAllUsers(): List<User> {
        return userRepository.findAll()
    }
}
