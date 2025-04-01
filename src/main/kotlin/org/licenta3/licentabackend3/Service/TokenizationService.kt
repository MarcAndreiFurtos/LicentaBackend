package org.licenta3.licentabackend3.Service

import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

@Service
class TokenizationService {

    private val keySize = 256
    private val gcmTagLength = 128
    private val ivSize = 12 // Recommended IV size for GCM
    private val secretKey: SecretKey = generateSecretKey()

    fun tokenize(cardNumber: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val iv = ByteArray(ivSize)
        SecureRandom().nextBytes(iv)

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(gcmTagLength, iv))
        val encryptedBytes = cipher.doFinal(cardNumber.toByteArray())

        // Store IV and encrypted data together (Base64 encoded)
        val ivAndEncryptedData = iv + encryptedBytes
        return Base64.getEncoder().encodeToString(ivAndEncryptedData)
    }

    fun detokenize(token: String): String {
        val decodedBytes = Base64.getDecoder().decode(token)

        val iv = decodedBytes.copyOfRange(0, ivSize)
        val encryptedData = decodedBytes.copyOfRange(ivSize, decodedBytes.size)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(gcmTagLength, iv))

        return String(cipher.doFinal(encryptedData))
    }

    private fun generateSecretKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(keySize)
        return keyGenerator.generateKey()
    }
}
