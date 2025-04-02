package org.licenta3.licentabackend3.Service

import jakarta.transaction.Transactional

import org.licenta3.licentabackend3.Entities.User
import org.licenta3.licentabackend3.Repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(private val userRepository: UserRepository) {

    fun createUser(user: User): User {
        return userRepository.save(user)
    }

    fun getUserById(id: Long): User {
        return userRepository.findById(id).orElseThrow {
            throw RuntimeException("User not found with ID: $id")
        }
    }

    fun updateUser(id: Long, user: User): User {
        val existingUser = userRepository.findById(id).orElseThrow {
            throw RuntimeException("User not found with ID: $id")
        }

        existingUser.apply {
            email = user.email
            password = user.password
            address = user.address
            profilePicture = user.profilePicture
            tokenizedCards = user.tokenizedCards
        }

        return userRepository.save(existingUser)
    }

    @Transactional
    fun deleteUser(id: Long) {
        userRepository.deleteById(id)
    }

    fun getAllUsers(): List<User> {
        return userRepository.findAll()
    }
}
