package com.wavesenterprise.sdk.contract.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import com.wavesenterprise.sdk.contract.core.converter.factory.ConverterFactory

class JacksonConverterFactory(
    private val objectMapper: ObjectMapper,
) : ConverterFactory {

    override fun toDataValueConverter() = JacksonContractToDataValueConverter(objectMapper)

    override fun fromDataEntryConverter() = JacksonFromDataEntryConverter(objectMapper)
}
