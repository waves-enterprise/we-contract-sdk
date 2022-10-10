package my.sample.kotlin.contract.rockps.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties("node")
@ConstructorBinding
data class NodeProperties(
    val credentials: Credentials,
    val feign: Feign,
) {

    data class Credentials(
        val senderAddress: String,
        val password: String = "",
    )

    data class Feign(
        val url: String,
        val decode404: Boolean = true,
        val connectTimeout: Long = 5000,
        val readTimeout: Long = 3000,
    )
}
