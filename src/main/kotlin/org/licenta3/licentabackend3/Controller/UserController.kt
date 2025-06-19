package org.licenta3.licentabackend3.Controller


import org.licenta3.licentabackend3.DTO.ProfilePictureDto
import org.licenta3.licentabackend3.DTO.UserDto
import org.licenta3.licentabackend3.Entities.User
import org.licenta3.licentabackend3.Service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(private val userService: UserService) {

    @PostMapping
    fun createUser(@RequestBody user: UserDto): ResponseEntity<User> {
        val savedUser = userService.createUser(user)
        return ResponseEntity.ok(savedUser)
    }

    @PutMapping("/profilePicture")
    fun updateUserProfilePicture (@RequestBody profilePictureDto: ProfilePictureDto): ResponseEntity<User> {
        userService.updateUserProfilePicture(profilePictureDto.userId, profilePictureDto.incriptedImmage)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: Long): ResponseEntity<User> {
        val user = userService.getUserById(id)
        return ResponseEntity.ok(user)
    }

    @PutMapping("/{id}")
    fun updateUser(@PathVariable id: Long, @RequestBody user: UserDto): ResponseEntity<User> {
        val updatedUser = userService.updateUser(id, user)
        return ResponseEntity.ok(updatedUser)
    }

    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: Long): ResponseEntity<Void> {
        userService.deleteUser(id)
        return ResponseEntity.noContent().build()
    }
    @GetMapping("/email/{email}")
    fun getUserByEmail(@PathVariable email: String): ResponseEntity<User> {
        val user = userService.getUserByEmail(email)
        if (user == null) {
            return ResponseEntity.notFound().build()
        }
        return ResponseEntity.ok(user)
    }

    @GetMapping
    fun getAllUsers(): ResponseEntity<List<User>> {
        val users = userService.getAllUsers()
        return ResponseEntity.ok(users)
    }
}
