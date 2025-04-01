package org.licenta3.licentabackend3.Service


import jakarta.transaction.Transactional
import org.licenta3.licentabackend3.DTO.SgrPickupETAResponseDTO
import org.licenta3.licentabackend3.Entities.SgrPickup
import org.licenta3.licentabackend3.Entities.SgrPickupStatus
import org.licenta3.licentabackend3.Repository.SgrPickupRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.math.BigDecimal

const val PACKING_EFFICIENCY = 0.8
const val BOTTLE_VOLUME = 1.0
const val CAN_VOLUME = 0.33
const val BOTTLE_RATIO = 0.5
const val ITEM_WORTH = 0.5
@Service
class SgrPickupService(
    private val sgrPickupRepository: SgrPickupRepository,
    private val restTemplate: RestTemplate,
    private val wisePaymentService: WisePaymentService
) {
    @Value("\${google.maps.api.key}")
    private lateinit var googleApiKey: String

    fun calculateETA(mPickup: String, destination: String): SgrPickupETAResponseDTO {
        val url = "https://maps.googleapis.com/maps/api/distancematrix/json" +
                "?origins=$mPickup&destinations=$destination&key=$googleApiKey"

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

    fun saveSgrPickup(mPickup: String, destination: String, sackVolume: Int): SgrPickup {
        val etaResponse = calculateETA(mPickup, destination)
        val sgrPickup = SgrPickup(
            mPickup = mPickup,
            destination = destination,
            value = estimateSackValue(sackVolume),
            estimatedTime = etaResponse.estimatedTime
        )
        return sgrPickupRepository.save(sgrPickup)
    }
    fun estimateSackValue(sackVolume: Int): Double {
        val volumeDouble = sackVolume.toDouble()
        val effectiveVolume = volumeDouble * PACKING_EFFICIENCY
        val avgItemVolume = BOTTLE_RATIO * BOTTLE_VOLUME + (1 - BOTTLE_RATIO) * CAN_VOLUME
        val estimatedItemCount = (effectiveVolume / avgItemVolume).toInt()
        return estimatedItemCount * ITEM_WORTH
    }

    @Transactional
    fun completeSgrPickup(id: Long): SgrPickup {
        val sgrPickup = sgrPickupRepository.findById(id).orElseThrow {
            throw RuntimeException("SgrPickup not found with ID: $id")
        }
        sgrPickup.status = SgrPickupStatus.COMPLETED
        return sgrPickupRepository.save(sgrPickup).also {
            processPaymentIfEligible(it)
        }
    }
    @Transactional
    fun markAsPaid(id: Long): SgrPickup {
        val sgrPickup = sgrPickupRepository.findById(id).orElseThrow {
            throw RuntimeException("SgrPickup not found with ID: $id")
        }

        sgrPickup.paidFor = true
        return sgrPickupRepository.save(sgrPickup).also {
            processPaymentIfEligible(it)
        }
    }
    private fun processPaymentIfEligible(sgrPickup: SgrPickup) {
        if (sgrPickup.status == SgrPickupStatus.COMPLETED && sgrPickup.paidFor) {
            val amountToPay = sgrPickup.value * 0.50 // 50% of value
            val transferId = wisePaymentService.transferToIban(BigDecimal(amountToPay))
            println("IBAN Transfer Successful: Transfer ID = $transferId")
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

    fun getCompletedSgrPickups(): List<SgrPickup> {
        return sgrPickupRepository.findByStatus(SgrPickupStatus.COMPLETED)
    }

    fun getCanceledSgrPickups(): List<SgrPickup> {
        return sgrPickupRepository.findByStatus(SgrPickupStatus.CANCELLED)
    }
}
