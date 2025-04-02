package org.licenta3.licentabackend3.Repository

import org.licenta3.licentabackend3.Entities.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long>
