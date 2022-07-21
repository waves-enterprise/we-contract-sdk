package my.sample.kotlin.contract.rockps

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.wavesenterprise.sdk.contract.grpc.GrpcJacksonContractDispatcherBuilder

class MainDispatch {
    fun main(args: Array<String>) {
        val contractDispatcher = GrpcJacksonContractDispatcherBuilder.builder()
            .contractHandlerType(RockPaperScissorsContractImpl::class.java)
            .objectMapper(objectMapper)
            .build()
        contractDispatcher.dispatch()
    }

    private val objectMapper: ObjectMapper
        get() = JsonMapper.builder().addModule(JavaTimeModule()).build()
}