package com.healthcare.play.security.crypto

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*

@Converter
@Component
class EncryptedStringConverter @Autowired constructor(
    private val props: CryptoProps
) : AttributeConverter<String?, String?> {
    override fun convertToDatabaseColumn(attribute: String?): String? {
        if (attribute.isNullOrEmpty()) return attribute
        val bytes = AesGcm.encrypt(props.aesKeyBase64, attribute.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(bytes)
    }
    override fun convertToEntityAttribute(dbData: String?): String? {
        if (dbData.isNullOrEmpty()) return dbData
        val bytes = Base64.getDecoder().decode(dbData)
        val pt = AesGcm.decrypt(props.aesKeyBase64, bytes)
        return pt.toString(Charsets.UTF_8)
    }
}