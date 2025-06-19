package org.licenta3.licentabackend3.Controller

import org.licenta3.licentabackend3.DTO.CardDto
import org.licenta3.licentabackend3.DTO.ConfirmationLinkDto
import org.licenta3.licentabackend3.DTO.SgrPickupDto
import org.licenta3.licentabackend3.Entities.TokenizedCard
import org.licenta3.licentabackend3.Repository.UserRepository
import org.licenta3.licentabackend3.Service.CardService
import org.licenta3.licentabackend3.service.StripePaymentService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/stripe")
@CrossOrigin(origins = ["*"] )
class StripeController(private val stripePaymentService: StripePaymentService, private val userRepository: UserRepository) {

    @PostMapping("/{id}")
    fun createStripeAccount(@PathVariable id: Long): ResponseEntity<String> {
        val user = userRepository.findById(id).orElseThrow { RuntimeException("User not found") }
        val response = stripePaymentService.createConnectedAccountWithDetails(user.email,user.firstName,user.lastName,"RO")
        val accountId = response["accountId"]
        user.connectedAccount = accountId.toString()
        userRepository.save(user)
        return  ResponseEntity.ok(accountId.toString())
    }

    @PutMapping("/{id}")
    fun getConfirmationLink(@PathVariable id: Long,@RequestBody confirmationLinkDto: ConfirmationLinkDto): ResponseEntity<String> {
        val user = userRepository.findById(id).orElseThrow { RuntimeException("User not found") }
        if (user.connectedAccount == ""){
            return ResponseEntity("You are not connected to this account",HttpStatus.NO_CONTENT)
        }
        val response = stripePaymentService.createAccountLink(user.connectedAccount,confirmationLinkDto.refreshUrl,confirmationLinkDto.returnUrl)
        return ResponseEntity.ok(response.toString())
    }

}