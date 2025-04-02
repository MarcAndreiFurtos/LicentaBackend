package org.licenta3.licentabackend3.Service

import jakarta.transaction.Transactional
import org.licenta3.licentabackend3.DTO.UserDto
import org.licenta3.licentabackend3.Entities.Address
import org.licenta3.licentabackend3.Entities.ProfilePicture
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

        val profilePicture = userDto.profilePicture?.let {
            ProfilePicture(incriptedImmage = it.incriptedImmage)
        }


        val user = User(
            email = userDto.email,
            password = userDto.password,
            address = address,
            profilePicture = profilePicture,
            rating = userDto.rating
        )

        return userRepository.save(user)
    }

    fun getUserById(id: Long): User {
        return userRepository.findById(id).orElseThrow {
            RuntimeException("User not found with ID: $id")
        }
    }

    @Transactional
    fun updateUser(id: Long, userDto: UserDto): User {
        val existingUser = getUserById(id)
        existingUser.email = userDto.email
        existingUser.password = userDto.password
        existingUser.address = Address(
            street = userDto.address.street,
            city = userDto.address.city,
            postalCode = userDto.address.postalCode,
            country = userDto.address.country
        )
        userDto.profilePicture?.let {
            existingUser.profilePicture = ProfilePicture(incriptedImmage = it.incriptedImmage)
        }
        existingUser.rating = userDto.rating
        return userRepository.save(existingUser)
    }

    fun deleteUser(id: Long) {
        userRepository.deleteById(id)
    }

    fun getAllUsers(): List<User> {
        return userRepository.findAll()
    }
}
