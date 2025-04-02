package org.licenta3.licentabackend3.Service

import org.licenta3.licentabackend3.DTO.CardDto
import org.licenta3.licentabackend3.Entities.TokenizedCard
import org.licenta3.licentabackend3.Repository.TokenizedCardRepository
import org.licenta3.licentabackend3.Repository.UserRepository
import org.springframework.stereotype.Service

@Service
class CardService(
    private val tokenizationService: TokenizationService,
    private val cardRepository: TokenizedCardRepository,
    private val userRepository: UserRepository
) {

    fun tokenizeAndSaveCard(cardDto: CardDto): TokenizedCard {
        val token = tokenizationService.tokenize(cardDto.cardNumber)
        val hashedExpirationDate = TokenizedCard.hashExpirationDate(cardDto.expirationDate)
        val user = userRepository.findById(cardDto.userId).orElseThrow{RuntimeException("User not found")}
        val tokenizedCard = TokenizedCard(
            token = token,
            cardholderName = cardDto.cardholderName,
            hashedExpirationDate = hashedExpirationDate,
            user = user
        )
        return cardRepository.save(tokenizedCard)
    }

    fun getCardByToken(token: String): TokenizedCard? {
        return cardRepository.findByToken(token)
    }

    fun deleteCard(id: Long) {
        cardRepository.deleteById(id)
    }
}
