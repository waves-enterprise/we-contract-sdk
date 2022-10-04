package com.wavesenterprise.sdk.contract.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import com.wavesenterprise.sdk.contract.api.state.ContractFromDataEntryConverter
import com.wavesenterprise.sdk.contract.api.state.TypeReference
import com.wavesenterprise.sdk.node.domain.DataEntry
import com.wavesenterprise.sdk.node.domain.DataValue

class JacksonFromDataEntryConverter(
    private val objectMapper: ObjectMapper,
) : ContractFromDataEntryConverter {
    override fun <T> convert(dataEntry: DataEntry, valueType: Class<T>): T = dataEntry.value.let {
        when (it) {
            is DataValue.StringDataValue -> convertFromString(it, valueType)
            is DataValue.IntegerDataValue -> convertFromInteger(it, valueType)
            is DataValue.BooleanDataValue -> convertFromBoolean(it, valueType)
            is DataValue.BinaryDataValue -> convertFromBinary(it, valueType)
        }
    }

    override fun <T> convert(dataEntry: DataEntry, typeReference: TypeReference<T>): T = dataEntry.value.let {
        when (it) {
            is DataValue.StringDataValue -> objectMapper.readValue<T>(
                it.value,
                objectMapper.typeFactory.constructType(typeReference.getType())
            )
            else -> throw IllegalArgumentException("Only string DataEntry can be converted to TypeReference")
        }
    }

    private fun <T> convertFromBinary(dataEntryValue: DataValue.BinaryDataValue, valueType: Class<T>): T {
        TODO("Not yet implemented")
    }

    private fun <T> convertFromBoolean(dataEntryValue: DataValue.BooleanDataValue, valueType: Class<T>): T {
        TODO("Not yet implemented")
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> convertFromInteger(dataEntryValue: DataValue.IntegerDataValue, valueType: Class<T>): T {
        val supportedTypes = setOf(
            Long::class.java,
            Int::class.java,
            Integer::class.java,
            java.lang.Long::class.java,
        )
        require(supportedTypes.any { it == valueType }) {
            "Only Integer or Long types are supported for integer DataEntry value conversion"
        }
        return when (valueType) {
            Long::class.java -> dataEntryValue.value as T
            Int::class.java -> dataEntryValue.value.toInt() as T
            Integer::class.java -> dataEntryValue.value.toInt() as T
            java.lang.Long::class.java -> dataEntryValue.value as T
            else -> {
                throw IllegalStateException("Unknown valueType = '$valueType'. See on supported types: $supportedTypes")
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> convertFromString(value: DataValue.StringDataValue, valueType: Class<T>): T =
        if (valueType == String::class.java) value.value as T else objectMapper.readValue(value.value, valueType)
}
