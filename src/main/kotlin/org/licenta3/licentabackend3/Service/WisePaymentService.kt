package org.licenta3.licentabackend3.Service


import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.http.*
import java.math.BigDecimal

@Service
class WisePaymentService(
    private val restTemplate: RestTemplate,
    private val objectMapper: ObjectMapper
) {

    @Value("\${wise.api.token}")
    private lateinit var apiToken: String

    @Value("\${wise.api.profileId}")
    private lateinit var profileId: String

    @Value("\${wise.api.url}")
    private lateinit var wiseApiUrl: String

    @Value("\${wise.currency}")
    private lateinit var currency: String

    private val iban = "YOUR_RECIPIENT_IBAN_HERE" // Replace with actual IBAN

    fun transferToIban(amount: BigDecimal): String {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.set("Authorization", "Bearer $apiToken")

        // 1️⃣ Create Quote (Wise requires a quote before making a transfer)
        val quoteRequest = mapOf(
            "profile" to profileId,
            "source" to currency,
            "target" to currency,
            "rateType" to "FIXED",
            "targetAmount" to amount,
            "type" to "BALANCE_PAYOUT"
        )

        val quoteResponse = restTemplate.exchange(
            "$wiseApiUrl/v3/quotes",
            HttpMethod.POST,
            HttpEntity(quoteRequest, headers),
            String::class.java
        )

        val quoteId = objectMapper.readTree(quoteResponse.body)["id"].asText()

        // 2️⃣ Create Recipient
        val recipientRequest = mapOf(
            "profile" to profileId,
            "accountHolderName" to "Recipient Name",
            "currency" to currency,
            "type" to "iban",
            "details" to mapOf("iban" to iban)
        )

        val recipientResponse = restTemplate.exchange(
            "$wiseApiUrl/v1/accounts",
            HttpMethod.POST,
            HttpEntity(recipientRequest, headers),
            String::class.java
        )

        val recipientId = objectMapper.readTree(recipientResponse.body)["id"].asText()

        // 3️⃣ Create Transfer
        val transferRequest = mapOf(
            "targetAccount" to recipientId,
            "quoteUuid" to quoteId,
            "customerTransactionId" to java.util.UUID.randomUUID().toString(),
            "details" to mapOf("reference" to "SgrPickup Payment")
        )

        val transferResponse = restTemplate.exchange(
            "$wiseApiUrl/v1/transfers",
            HttpMethod.POST,
            HttpEntity(transferRequest, headers),
            String::class.java
        )

        val transferId = objectMapper.readTree(transferResponse.body)["id"].asText()

        // 4️⃣ Fund Transfer
        val fundTransferRequest = mapOf("type" to "BALANCE")

        restTemplate.exchange(
            "$wiseApiUrl/v3/profiles/$profileId/transfers/$transferId/payments",
            HttpMethod.POST,
            HttpEntity(fundTransferRequest, headers),
            String::class.java
        )

        return transferId
    }
}
