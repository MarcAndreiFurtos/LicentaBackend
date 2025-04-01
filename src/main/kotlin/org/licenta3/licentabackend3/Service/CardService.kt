package org.licenta3.licentabackend3.Service

import org.licenta3.licentabackend3.DTO.CardDto
import org.licenta3.licentabackend3.Entities.TokenizedCard
import org.licenta3.licentabackend3.Repository.TokenizedCardRepository
import org.springframework.stereotype.Service

@Service
class CardService(
    private val tokenizationService: TokenizationService,
    private val cardRepository: TokenizedCardRepository
) {

    fun tokenizeAndSaveCard(cardDto: CardDto): TokenizedCard {
        val token = tokenizationService.tokenize(cardDto.cardNumber)
        val hashedExpirationDate = TokenizedCard.hashExpirationDate(cardDto.expirationDate)

        val tokenizedCard = TokenizedCard(
            token = token,
            cardholderName = cardDto.cardholderName,
            hashedExpirationDate = hashedExpirationDate
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
