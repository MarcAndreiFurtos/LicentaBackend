package org.licenta3.licentabackend3.Mappers

import org.licenta3.licentabackend3.DTO.CardDto
import org.licenta3.licentabackend3.Entities.TokenizedCard
import org.springframework.stereotype.Component

@Component
class CardMapper {
    fun maptoDto(card: TokenizedCard):CardDto{
        val userId = card.user.id
        return userId?.let { CardDto(card.token,card.cardholderName,card.accountId, it) }!!
    }
}