package com.wavesenterprise.sdk.contract.grpc.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import com.wavesenterprise.sdk.contract.api.state.ContractToDataValueConverter
import com.wavesenterprise.sdk.node.domain.DataValue

class JacksonContractToDataValueConverter(
    private val objectMapper: ObjectMapper,
) : ContractToDataValueConverter {

    override fun <T> convert(value: T): DataValue = when (value) {
        is Boolean -> DataValue.BooleanDataValue(value)
        is String -> DataValue.StringDataValue(value)
        is Int -> DataValue.IntegerDataValue(value.toLong())
        is Long -> DataValue.IntegerDataValue(value)
        else -> DataValue.StringDataValue(objectMapper.writeValueAsString(value))
    }
}
