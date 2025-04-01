package org.licenta3.licentabackend3.Repository

import org.licenta3.licentabackend3.Entities.TokenizedCard
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TokenizedCardRepository : JpaRepository<TokenizedCard, Long> {
    fun findByToken(token: String): TokenizedCard?
}
