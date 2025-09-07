package com.healthcare.play.security.crypto

import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random

object AesGcm {
    fun encrypt(base64Key: String, plaintext: ByteArray): ByteArray {
        val key = SecretKeySpec(Base64.getDecoder().decode(base64Key), "AES")
        val iv = Random.Default.nextBytes(12)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, iv))
        val ct = cipher.doFinal(plaintext)
        return iv + ct
    }
    fun decrypt(base64Key: String, data: ByteArray): ByteArray {
        val key = SecretKeySpec(Base64.getDecoder().decode(base64Key), "AES")
        val iv = data.copyOfRange(0, 12)
        val ct = data.copyOfRange(12, data.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
        return cipher.doFinal(ct)
    }
}