package org.licenta3.licentabackend3.Controller

import org.licenta3.licentabackend3.DTO.CardDto
import org.licenta3.licentabackend3.Entities.TokenizedCard
import org.licenta3.licentabackend3.Service.CardService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/cards")
class CardController(private val cardService: CardService) {

    @PostMapping("/tokenize")
    fun tokenizeCard(@RequestBody cardDto: CardDto): ResponseEntity<TokenizedCard> {
        val tokenizedCard = cardService.tokenizeAndSaveCard(cardDto)
        return ResponseEntity.ok(tokenizedCard)
    }

    @GetMapping("/{token}")
    fun getCardByToken(@PathVariable token: String): ResponseEntity<TokenizedCard> {
        val card = cardService.getCardByToken(token)
        return if (card != null) {
            ResponseEntity.ok(card)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/{id}")
    fun deleteCard(@PathVariable id: Long): ResponseEntity<Void> {
        cardService.deleteCard(id)
        return ResponseEntity.noContent().build()
    }
}
