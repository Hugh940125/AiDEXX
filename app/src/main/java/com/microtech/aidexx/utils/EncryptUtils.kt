package com.microtech.aidexx.utils

import org.apache.commons.codec.binary.Base64
import org.apache.commons.codec.binary.Hex
import org.apache.commons.codec.digest.DigestUtils

class EncryptUtils {
    companion object {
        fun md5(input: String?): String {
            return String(Hex.encodeHex(DigestUtils.md5(input)))
        }

        fun encodeBase64(byte: ByteArray?): String? {
            return if (byte != null) String(Base64.encodeBase64(byte), Charsets.UTF_8) else null
        }
    }
}