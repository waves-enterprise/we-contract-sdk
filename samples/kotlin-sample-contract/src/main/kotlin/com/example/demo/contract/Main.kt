package com.example.demo.contract

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.wavesenterprise.sdk.contract.grpc.GrpcJacksonContractDispatcherBuilder

fun main() {
    val dispatcher = GrpcJacksonContractDispatcherBuilder()
        .contractHandlerType(PersonContractHandler::class.java)
        .objectMapper(jacksonObjectMapper())
        .build()
    dispatcher.dispatch()
}