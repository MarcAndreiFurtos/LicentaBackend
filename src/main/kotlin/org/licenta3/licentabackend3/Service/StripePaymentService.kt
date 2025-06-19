package org.licenta3.licentabackend3.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.math.BigDecimal

@Service
class StripePaymentService(
    private val restTemplate: RestTemplate,
    private val objectMapper: ObjectMapper
) {

    @Value("\${stripe.api.secretKey}")
    private lateinit var secretKey: String

    @Value("\${stripe.api.baseUrl}")
    private lateinit var stripeApiUrl: String

    @Value("\${app.base.url:http://localhost:8080}")
    private lateinit var baseUrl: String

    private fun createHeaders(): HttpHeaders {
        return HttpHeaders().apply {
            contentType = MediaType.APPLICATION_FORM_URLENCODED
            set("Authorization", "Bearer $secretKey")
        }
    }

    fun createConnectedAccount(
        email: String,
        country: String = "US",
        businessType: String = "individual",
        accountType: String = "express"
    ): String {
        val headers = createHeaders()

        val accountRequest = "type=$accountType" +
                "&country=$country" +
                "&email=$email" +
                "&business_type=$businessType" +
                "&capabilities[card_payments][requested]=true" +
                "&capabilities[transfers][requested]=true"

        val requestEntity = HttpEntity(accountRequest, headers)
        val response = restTemplate.postForEntity("$stripeApiUrl/v1/accounts", requestEntity, String::class.java)

        return objectMapper.readTree(response.body)["id"].asText()
    }

    fun createConnectedAccountWithDetails(
        email: String,
        firstName: String,
        lastName: String,
        country: String = "US",
        businessType: String = "individual"
    ): Map<String, Any> {
        val headers = createHeaders()

        val accountRequest = "type=express" +
                "&country=$country" +
                "&email=$email" +
                "&business_type=$businessType" +
                "&individual[first_name]=$firstName" +
                "&individual[last_name]=$lastName" +
                "&capabilities[card_payments][requested]=true" +
                "&capabilities[transfers][requested]=true"

        val requestEntity = HttpEntity(accountRequest, headers)
        val response = restTemplate.postForEntity("$stripeApiUrl/v1/accounts", requestEntity, String::class.java)

         val responseBody = objectMapper.readTree(response.body)

        return mapOf(
            "accountId" to responseBody["id"].asText(),
            "email" to responseBody["email"].asText(),
            "detailsSubmitted" to responseBody["details_submitted"].asBoolean(),
            "chargesEnabled" to responseBody["charges_enabled"].asBoolean(),
            "payoutsEnabled" to responseBody["payouts_enabled"].asBoolean()
        )
    }

    fun createAccountLink(accountId: String, refreshUrl: String, returnUrl: String, type: String = "account_onboarding"): String {
        val headers = createHeaders()

        val linkRequest = "account=$accountId" +
                "&refresh_url=$refreshUrl" +
                "&return_url=$returnUrl" +
                "&type=$type"

        val requestEntity = HttpEntity(linkRequest, headers)
        val response = restTemplate.postForEntity("$stripeApiUrl/v1/account_links", requestEntity, String::class.java)

        return objectMapper.readTree(response.body)["url"].asText()
    }

    fun getConnectedAccountStatus(accountId: String): Map<String, Any> {
        val headers = createHeaders()
        val requestEntity = HttpEntity<Any>(null, headers)

        val response = restTemplate.exchange(
            "$stripeApiUrl/v1/accounts/$accountId",
            HttpMethod.GET,
            requestEntity,
            String::class.java
        )

        val responseBody = objectMapper.readTree(response.body)

        return mapOf(
            "id" to responseBody["id"].asText(),
            "email" to (responseBody["email"]?.asText() ?: ""),
            "detailsSubmitted" to responseBody["details_submitted"].asBoolean(),
            "chargesEnabled" to responseBody["charges_enabled"].asBoolean(),
            "payoutsEnabled" to responseBody["payouts_enabled"].asBoolean(),
            "country" to responseBody["country"].asText(),
            "defaultCurrency" to responseBody["default_currency"].asText()
        )
    }

    fun transferToConnectedAccount(amount: BigDecimal, currency: String, recipientCardToken: String, recipientName: String): String {
        val headers = createHeaders()

        val amountInCents = (amount * BigDecimal(100)).toLong()

        val transferRequest = "amount=$amountInCents" +
                "&currency=$currency" +
                "&destination=$recipientCardToken" +
                "&description=Transfer to $recipientName"

        val requestEntity = HttpEntity(transferRequest, headers)
        val response = restTemplate.postForEntity("$stripeApiUrl/v1/transfers", requestEntity, String::class.java)

        return objectMapper.readTree(response.body)["id"].asText()
    }

    fun receivePayment(amount: Double, currency: String, paymentMethodToken: String, customerId: String): String {
        val paymentIntentId = createPaymentIntent(amount, currency, paymentMethodToken, customerId )

        confirmPayment(paymentIntentId, paymentMethodToken)

        return paymentIntentId
    }

    fun receivePaymentWithTestCard(amount: Double, currency: String, testCardToken: String = "pm_card_visa", customerId: String): String {
        val paymentIntentId = createPaymentIntent(amount, currency, testCardToken, customerId )
        confirmPayment(paymentIntentId, testCardToken)
        return paymentIntentId
    }

    fun createTestPaymentMethod(testCardType: String = "visa"): String {
        val headers = createHeaders()

        val cardToken = when(testCardType.lowercase()) {
            "visa" -> "pm_card_visa"
            "visa_debit" -> "pm_card_visa_debit"
            "mastercard" -> "pm_card_mastercard"
            "amex" -> "pm_card_amex"
            "declined" -> "pm_card_visa_chargeDeclined"
            else -> "pm_card_visa"
        }

        return cardToken
    }

    fun createPaymentMethodFromToken(token: String, billingDetails: Map<String, String>? = null): String {
        val headers = createHeaders()

        var paymentMethodRequest = "type=card&card[token]=$token"

        billingDetails?.let { details ->
            details["name"]?.let { name -> paymentMethodRequest += "&billing_details[name]=$name" }
            details["email"]?.let { email -> paymentMethodRequest += "&billing_details[email]=$email" }
        }

        val requestEntity = HttpEntity(paymentMethodRequest, headers)
        val response = restTemplate.postForEntity("$stripeApiUrl/v1/payment_methods", requestEntity, String::class.java)

        return objectMapper.readTree(response.body)["id"].asText()
    }

    private fun createPaymentIntent(amount: Double, currency: String, paymentMethodId: String, customerId: String): String {
        val headers = createHeaders()

        val amountInCents = (amount * 100).toLong()

        val paymentIntentRequest = "amount=$amountInCents" +
                "&currency=$currency" +
                "&payment_method=$paymentMethodId" +
                "&customer=$customerId" +
                "&confirm=false" +
                "&automatic_payment_methods[enabled]=true" +
                "&automatic_payment_methods[allow_redirects]=never"

        val requestEntity = HttpEntity(paymentIntentRequest, headers)
        val response = restTemplate.postForEntity("$stripeApiUrl/v1/payment_intents", requestEntity, String::class.java)

        return objectMapper.readTree(response.body)["id"].asText()
    }

    private fun createPaymentIntentWithRedirects(amount: Double, currency: String, paymentMethodId: String, customerId: String): String {
        val headers = createHeaders()

        val amountInCents = (amount * 100).toLong()

        val paymentIntentRequest = "amount=$amountInCents" +
                "&currency=$currency" +
                "&payment_method=$paymentMethodId" +
                "&customer=$customerId" +
                "&confirmation_method=manual" +
                "&confirm=false" +
                "&automatic_payment_methods[enabled]=true" +
                "&automatic_payment_methods[allow_redirects]=always" +
                "&return_url=$baseUrl/api/payment/return"

        val requestEntity = HttpEntity(paymentIntentRequest, headers)
        val response = restTemplate.postForEntity("$stripeApiUrl/v1/payment_intents", requestEntity, String::class.java)

        return objectMapper.readTree(response.body)["id"].asText()
    }

    private fun confirmPayment(paymentIntentId: String, paymentMethodId: String): String {
        val headers = createHeaders()

        val confirmRequest = "payment_method=$paymentMethodId" +
                "&return_url=$baseUrl/api/payment/return"

        val requestEntity = HttpEntity(confirmRequest, headers)
        val response = restTemplate.postForEntity("$stripeApiUrl/v1/payment_intents/$paymentIntentId/confirm", requestEntity, String::class.java)

        return objectMapper.readTree(response.body)["status"].asText()
    }

    fun getPaymentStatus(paymentIntentId: String): String {
        val headers = createHeaders()
        val requestEntity = HttpEntity<Any>(null, headers)

        val response = restTemplate.exchange(
            "$stripeApiUrl/v1/payment_intents/$paymentIntentId",
            HttpMethod.GET,
            requestEntity,
            String::class.java
        )

        return objectMapper.readTree(response.body)["status"].asText()
    }

    fun getTransferStatus(transferId: String): String {
        val headers = createHeaders()
        val requestEntity = HttpEntity<Any>(null, headers)

        val response = restTemplate.exchange(
            "$stripeApiUrl/v1/transfers/$transferId",
            HttpMethod.GET,
            requestEntity,
            String::class.java
        )

        return objectMapper.readTree(response.body)["status"].asText()
    }

    fun createTestCustomer(email: String, name: String, testCardType: String = "visa"): String {
        val headers = createHeaders()

        val customerRequest = "email=$email&name=$name"

        val requestEntity = HttpEntity(customerRequest, headers)
        val response = restTemplate.postForEntity("$stripeApiUrl/v1/customers", requestEntity, String::class.java)

        val customerId = objectMapper.readTree(response.body)["id"].asText()

        val paymentMethodId = createTestPaymentMethod(testCardType)
        return customerId
    }

    private fun attachPaymentMethodToCustomer(paymentMethodId: String, customerId: String) {
        val headers = createHeaders()

        val attachRequest = "customer=$customerId"

        val requestEntity = HttpEntity(attachRequest, headers)
        restTemplate.postForEntity("$stripeApiUrl/v1/payment_methods/$paymentMethodId/attach", requestEntity, String::class.java)
    }

    fun handlePaymentReturn(paymentIntentId: String): Map<String, Any> {
        val paymentStatus = getPaymentStatus(paymentIntentId)

        return mapOf(
            "payment_intent_id" to paymentIntentId,
            "status" to paymentStatus,
            "success" to (paymentStatus == "succeeded")
        )
    }
}