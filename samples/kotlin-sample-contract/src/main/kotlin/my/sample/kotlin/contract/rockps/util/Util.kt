package my.sample.kotlin.contract.rockps.util

import org.apache.commons.codec.digest.DigestUtils
import java.nio.charset.StandardCharsets

fun hash(value: String): String {
    val digest = DigestUtils.getSha256Digest()
    val hashBytes = digest.digest(
        value.toByteArray(StandardCharsets.UTF_8)
    )
    return bytesToHex(hashBytes)
}

private fun bytesToHex(hash: ByteArray): String {
    val hexString = StringBuilder(2 * hash.size)
    for (i in hash.indices) {
        val hex = Integer.toHexString(0xff and hash[i].toInt())
        if (hex.length == 1) {
            hexString.append('0')
        }
        hexString.append(hex)
    }
    return hexString.toString()
}
