package org.licenta3.licentabackend3.Service

import jakarta.transaction.Transactional
import org.licenta3.licentabackend3.DTO.SgrPickupDto
import org.licenta3.licentabackend3.DTO.SgrPickupETAResponseDTO
import org.licenta3.licentabackend3.Entities.SgrPickup
import org.licenta3.licentabackend3.Entities.SgrPickupStatus
import org.licenta3.licentabackend3.Entities.TokenizedCard
import org.licenta3.licentabackend3.Entities.User
import org.licenta3.licentabackend3.Repository.SgrPickupRepository
import org.licenta3.licentabackend3.Repository.TokenizedCardRepository
import org.licenta3.licentabackend3.Repository.UserRepository
import org.licenta3.licentabackend3.service.StripePaymentService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.math.BigDecimal
import java.util.stream.Collectors

const val PACKING_EFFICIENCY = 0.8
const val BOTTLE_VOLUME = 1.0
const val CAN_VOLUME = 0.33
const val BOTTLE_RATIO = 0.5
const val ITEM_WORTH = 0.5

@Service
class SgrPickupService(
    private val sgrPickupRepository: SgrPickupRepository,
    private val restTemplate: RestTemplate,
    private val stripePaymentService: StripePaymentService,
    private val emailService: EmailService,
    private val userRepository: UserRepository,
    private val tokenizedCardRepository: TokenizedCardRepository,
    private val tokenizationService: TokenizationService
) {

    @Value("\${google.maps.api.key}")
    private lateinit var googleApiKey: String

    fun calculateETA(pickupId: Long): SgrPickupETAResponseDTO {
        val sgrPickup = sgrPickupRepository.findById(pickupId).orElseThrow {
            throw RuntimeException("SgrPickup not found with ID: $pickupId")
        }
        return calculateETASubFct(sgrPickup.driverLocation,sgrPickup.destination)
    }

    fun calculateETASubFct(driverLocation: String, destination: String): SgrPickupETAResponseDTO {
        val url = "https://maps.googleapis.com/maps/api/distancematrix/json" +
                "?origins=$driverLocation&destinations=$destination&key=$googleApiKey"

        val response = restTemplate.getForObject(url, Map::class.java) as Map<String, Any>
        val rows = response["rows"] as List<Map<String, Any>>
        val elements = rows[0]["elements"] as List<Map<String, Any>>
        val duration = elements[0]["duration"] as Map<String, Any>
        val distance = elements[0]["distance"] as Map<String, Any>

        return SgrPickupETAResponseDTO(
            estimatedTime = duration["text"] as String,
            distance = distance["text"] as String
        )
    }

    fun calculateDistance(mPickup: String, destination: String): String {
        val url = "https://maps.googleapis.com/maps/api/distancematrix/json" +
                "?origins=$mPickup&destinations=$destination&key=$googleApiKey"

        val response = restTemplate.getForObject(url, Map::class.java) as Map<String, Any>
        val rows = response["rows"] as List<Map<String, Any>>
        val elements = rows[0]["elements"] as List<Map<String, Any>>
        val distance = elements[0]["distance"] as Map<String, Any>

        return distance["text"] as String
    }

    fun getPickupStatus(pickupId:Long): SgrPickupStatus {
        val pickup = sgrPickupRepository.findById(pickupId).orElseThrow { RuntimeException("User not found") }
        return pickup.status
    }

    fun saveSgrPickup(mPickup: String, destination: String, sackVolume: Int, userId:Long): SgrPickup {
        val etaResponse = calculateETASubFct(mPickup, destination)
        val user = userRepository.findById(userId).orElseThrow { RuntimeException("User not found") }
        val sgrPickup = SgrPickup(
            driverLocation = mPickup,
            destination = destination,
            value = estimateSackValue(sackVolume),
            estimatedTime = etaResponse.estimatedTime,
            user = user,
//          user creates the pickup and has no  knowledge of who the driver will be at the creation time
            driver = user,
        )
        val savedPickup = sgrPickupRepository.save(sgrPickup)
        emailService.sendEmail(
            user.email,
            "New SgrPickup Created",
            "Your SgrPickup request $destination has been created. Our riders will soon pick up the order , we will be on our way soon :))" +
                    "Our Regards," +
                    "The SgrPickup Team"

        )

        return savedPickup
    }

    fun estimateSackValue(sackVolume: Int): Double {
        val volumeDouble = sackVolume.toDouble()
        val effectiveVolume = volumeDouble * PACKING_EFFICIENCY
        val avgItemVolume = BOTTLE_RATIO * BOTTLE_VOLUME + (1 - BOTTLE_RATIO) * CAN_VOLUME
        val estimatedItemCount = (effectiveVolume / avgItemVolume).toInt()
        return estimatedItemCount * ITEM_WORTH
    }

    @Transactional
    fun completeSgrPickup(id: Long,sgrPickupDto: SgrPickupDto): SgrPickup {
        val sgrPickup = sgrPickupRepository.findById(id).orElseThrow {
            throw RuntimeException("SgrPickup not found with ID: $id")
        }
        sgrPickup.status = SgrPickupStatus.COMPLETED
        return sgrPickupRepository.save(sgrPickup).also {
            processPaymentIfEligible(it,sgrPickupDto)
        }
    }

    @Transactional
    fun markAsPaid(id: Long,sgrPickupDto: SgrPickupDto): SgrPickup {
        val sgrPickup = sgrPickupRepository.findById(id).orElseThrow {
            throw RuntimeException("SgrPickup not found with ID: $id")
        }

        sgrPickup.paidFor = true
        return sgrPickupRepository.save(sgrPickup).also {
            processPaymentIfEligible(it,sgrPickupDto)
        }
    }

    @Transactional
    fun markInProgress(id: Long, sgrPickupDto: SgrPickupDto): SgrPickup {
        val sgrPickup = sgrPickupRepository.findById(id).orElseThrow {
            throw RuntimeException("SgrPickup not found with ID: $id")
        }
        val driver = userRepository.findById(sgrPickupDto.driverId).orElseThrow { RuntimeException("User not found") }
        val card = tokenizedCardRepository.findByUser(driver).stream().filter { it.id == sgrPickupDto.cardId }.collect(Collectors.toList())[0]
        sgrPickup.status = SgrPickupStatus.IN_PROGRESS
        sgrPickup.driver = driver
        sgrPickup.driverLocation = sgrPickupDto.driverLocation
        return sgrPickupRepository.save(sgrPickup).also {
            stripePaymentService.receivePayment(sgrPickup.value,"RON",tokenizationService.detokenize(card.token),card.accountId)
        }
    }

    @Transactional
    fun updateDriverLocation(id: Long,sgrPickupDto: SgrPickupDto): SgrPickup {
        val sgrPickup = sgrPickupRepository.findById(id).orElseThrow {
            throw RuntimeException("SgrPickup not found with ID: $id")
        }

        sgrPickup.driverLocation = sgrPickupDto.driverLocation
        return sgrPickupRepository.save(sgrPickup)
    }

    private fun processPaymentIfEligible(sgrPickup: SgrPickup, sgrPickupDto: SgrPickupDto) {
        if (sgrPickup.status == SgrPickupStatus.COMPLETED && sgrPickup.paidFor) {
            val amountToPay = sgrPickup.value * 0.50 // 50% of value
            val amountToPayDriver = sgrPickup.value * 0.25
            val user = userRepository.findById(sgrPickupDto.userId).orElseThrow { RuntimeException("User not found") }
            val driver = userRepository.findById(sgrPickupDto.driverId).orElseThrow { RuntimeException("User not found") }

            payUserOrDriver(user, amountToPay)
            payUserOrDriver(driver, amountToPayDriver)
        }
    }

    private fun payUserOrDriver(
        user: User,
        amountToPay: Double
    ) {
        if (user.connectedAccount.equals("")) {
//            val account =
//                stripePaymentService.createConnectedAccountWithDetails(user.email, user.firstName, user.lastName, "RO")
//            user.connectedAccount = account["accountId"].toString()
//            userRepository.save(user)
            throw RuntimeException("Connected Account not connected")
        }
        try {
            val payoutBatchId = stripePaymentService.transferToConnectedAccount(
                BigDecimal(amountToPay),
                "RON",
                user.connectedAccount,
                user.firstName
            )

            println("Payout batch initiated: Batch ID = $payoutBatchId")

            emailService.sendEmail(
                user.email,
                "Payment Processing",
                "Your payment of €${
                    String.format(
                        "%.2f",
                        amountToPay
                    )
                } is being processed. You will receive another notification once completed."
            )

        } catch (e: Exception) {
            println("Failed to initiate payout: ${e.message}")
        }
    }

    @Transactional
    fun cancelSgrPickup(id: Long): SgrPickup {
        val sgrPickup = sgrPickupRepository.findById(id).orElseThrow {
            throw RuntimeException("SgrPickup not found with ID: $id")
        }
        sgrPickup.status = SgrPickupStatus.CANCELLED
        return sgrPickupRepository.save(sgrPickup)
    }

    fun getCompletedSgrPickups(userId: Long): List<SgrPickup> {
        return sgrPickupRepository.findByStatus(SgrPickupStatus.COMPLETED).filter{it.user.id == userId}
    }

    fun getPendingSgrPickups(userId: Long): List<SgrPickup> {
        return sgrPickupRepository.findByStatus(SgrPickupStatus.PENDING).filter{it.user.id == userId}
    }

    fun getCanceledSgrPickups(userId: Long): List<SgrPickup> {
        return sgrPickupRepository.findByStatus(SgrPickupStatus.CANCELLED).filter{it.user.id == userId}
    }
}