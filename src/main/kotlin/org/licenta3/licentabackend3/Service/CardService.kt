package org.licenta3.licentabackend3.Service

import org.licenta3.licentabackend3.DTO.CardDto
import org.licenta3.licentabackend3.Entities.TokenizedCard
import org.licenta3.licentabackend3.Mappers.CardMapper
import org.licenta3.licentabackend3.Repository.TokenizedCardRepository
import org.licenta3.licentabackend3.Repository.UserRepository
import org.springframework.stereotype.Service
import java.util.stream.Collectors

@Service
class CardService(
    private val tokenizationService: TokenizationService,
    private val cardRepository: TokenizedCardRepository,
    private val userRepository: UserRepository,
    private val cardMapper: CardMapper
) {

    fun tokenizeAndSaveCard(cardDto: CardDto): TokenizedCard {
        val token = tokenizationService.tokenize(cardDto.cardNumber)
        val user = userRepository.findById(cardDto.userId).orElseThrow{RuntimeException("User not found")}
        val tokenizedCard = TokenizedCard(
            token = token,
            cardholderName = cardDto.cardholderName,
            accountId = cardDto.accountId,
            user = user
        )
        return cardRepository.save(tokenizedCard)
    }

    fun getCardByToken(token: String): TokenizedCard? {
        return cardRepository.findByToken(token)
    }

    fun getAllCardsByUser(userId:Long): List<CardDto> {
        val user = userRepository.findById(userId).orElseThrow{RuntimeException("User not found")}
        return cardRepository.findByUser(user).stream().map { t-> cardMapper.maptoDto(t) }.collect(Collectors.toList())!!
    }

    fun deleteCard(id: Long) {
        cardRepository.deleteById(id)
    }
}
