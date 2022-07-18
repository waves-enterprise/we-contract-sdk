package com.wavesenterprise.sdk.contract.grpc.jackson

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.wavesenterprise.sdk.contract.api.state.ContractToDataValueConverter
import com.wavesenterprise.sdk.contract.jackson.JacksonContractToDataValueConverter
import com.wavesenterprise.sdk.node.domain.DataValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.math.BigDecimal
import java.util.stream.Stream

internal class ContractToDataValueConverterTest {

    private val converter: ContractToDataValueConverter = JacksonContractToDataValueConverter(jacksonObjectMapper())

    @Test
    fun `should convert simple dto to string dataValue`() {
        val value = SimpleDto("name", 134)
        val result = converter.convert(value)

        assertTrue(result is DataValue.StringDataValue)
        assertEquals(value, jacksonObjectMapper().readValue<SimpleDto>((result as DataValue.StringDataValue).value))
    }

    @Test
    fun `should convert string to string dataValue`() {
        val value = "stringValue"
        val result = converter.convert(value)

        assertTrue(result is DataValue.StringDataValue)
        assertEquals(value, (result as DataValue.StringDataValue).value)
    }

    @Test
    fun `should convert BigDecimal to string dataValue`() {
        val value = BigDecimal("1.23")
        val result = converter.convert(value)

        assertTrue(result is DataValue.StringDataValue)
        assertEquals(value.toPlainString(), (result as DataValue.StringDataValue).value)
    }

    @ParameterizedTest
    @MethodSource("integerValues")
    fun `should convert to integer dataValue`(value: Number) {
        val result = converter.convert(value)

        assertTrue(result is DataValue.IntegerDataValue)
        assertEquals(value.toLong(), (result as DataValue.IntegerDataValue).value)
    }

    @Test
    fun `should convert boolean to boolean dataValue`() {
        val value = true
        val result = converter.convert(value)

        assertTrue(result is DataValue.BooleanDataValue)
        assertEquals(value, (result as DataValue.BooleanDataValue).value)
    }

    data class SimpleDto(
        val name: String,
        val age: Int,
    )

    companion object {
        @JvmStatic
        fun integerValues(): Stream<Arguments> = Stream.of(
            Arguments.of(1234L),
            Arguments.of(1234),
            Arguments.of(Integer.valueOf(123)),
            Arguments.of(java.lang.Long.valueOf(123)),
        )
    }
}
