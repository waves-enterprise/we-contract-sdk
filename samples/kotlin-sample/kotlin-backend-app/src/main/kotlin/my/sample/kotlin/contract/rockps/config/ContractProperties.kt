package my.sample.kotlin.contract.rockps.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties("contract")
@ConstructorBinding
data class ContractProperties(
    val senderAddress: String,
    val fee: Long,
    val contractVersion: Int,
    val version: Int,
    val image: String,
    val imageHash: String,
    val contractName: String,
)
