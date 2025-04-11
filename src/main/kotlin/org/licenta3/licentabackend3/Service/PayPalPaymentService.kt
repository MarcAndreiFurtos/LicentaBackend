package org.licenta3.licentabackend3.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.math.BigDecimal
import java.util.*

@Service
class PayPalPaymentService(
    private val restTemplate: RestTemplate,
    private val objectMapper: ObjectMapper
) {

    @Value("\${paypal.api.clientId}")
    private lateinit var clientId: String

    @Value("\${paypal.api.secret}")
    private lateinit var clientSecret: String

    @Value("\${paypal.api.baseUrl}")
    private lateinit var paypalApiUrl: String

    private fun getAccessToken(): String {
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_FORM_URLENCODED
            setBasicAuth(clientId, clientSecret)
        }
        val body = "grant_type=client_credentials"
        val request = HttpEntity(body, headers)
        val response = restTemplate.postForEntity("$paypalApiUrl/v1/oauth2/token", request, String::class.java)
        return objectMapper.readTree(response.body)["access_token"].asText()
    }

    fun transferToIban(amount: BigDecimal, currency: String, recipientIban: String, recipientName: String): String {
        // Note: PayPal Payouts API works with recipient emails.
        // In this function, we simulate transferring funds by using recipientIban as the receiver email.
        val accessToken = getAccessToken()
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            set("Authorization", "Bearer $accessToken")
        }
        val payoutRequest = mapOf(
            "sender_batch_header" to mapOf(
                "sender_batch_id" to UUID.randomUUID().toString(),
                "email_subject" to "You have a payout!",
                "email_message" to "You have received a payout from our service."
            ),
            "items" to listOf(
                mapOf(
                    "recipient_type" to "EMAIL",
                    "amount" to mapOf(
                        "value" to amount.toPlainString(),
                        "currency" to currency
                    ),
                    "receiver" to recipientIban, // using recipientIban as the receiver email here
                    "note" to "Payment to $recipientName",
                    "sender_item_id" to UUID.randomUUID().toString()
                )
            )
        )
        val requestEntity = HttpEntity(payoutRequest, headers)
        val response = restTemplate.postForEntity("$paypalApiUrl/v1/payments/payouts?sync_mode=true", requestEntity, String::class.java)
        return objectMapper.readTree(response.body)["batch_header"]["payout_batch_id"].asText()
    }

    fun receivePayment(amount: BigDecimal, currency: String, payerEmail: String): String {
        val accessToken = getAccessToken()
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            set("Authorization", "Bearer $accessToken")
        }
        val orderRequest = mapOf(
            "intent" to "CAPTURE",
            "purchase_units" to listOf(
                mapOf(
                    "amount" to mapOf(
                        "currency_code" to currency,
                        "value" to amount.toPlainString()
                    )
                )
            ),
            "payer" to mapOf(
                "email_address" to payerEmail
            )
        )
        val orderResponse = restTemplate.postForEntity("$paypalApiUrl/v2/checkout/orders", HttpEntity(orderRequest, headers), String::class.java)
        val orderId = objectMapper.readTree(orderResponse.body)["id"].asText()
        val captureResponse = restTemplate.postForEntity("$paypalApiUrl/v2/checkout/orders/$orderId/capture", HttpEntity(null, headers), String::class.java)
        return objectMapper.readTree(captureResponse.body)["purchase_units"][0]["payments"]["captures"][0]["id"].asText()
    }
}
