package my.sample.kotlin.contract.rockps

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.wavesenterprise.sdk.contract.grpc.GrpcJacksonContractDispatcherBuilder

fun main() {
    val contractDispatcher = GrpcJacksonContractDispatcherBuilder.builder()
        .contractHandlerType(RockPaperScissorsContractImpl::class.java)
        .objectMapper(objectMapper)
        .build()
    contractDispatcher.dispatch()
}

private val objectMapper = jacksonObjectMapper()
