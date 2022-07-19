package com.wavesenterprise.sdk.contract.grpc.jackson

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.wavesenterprise.sdk.contract.api.state.TypeReference
import com.wavesenterprise.sdk.contract.jackson.JacksonFromDataEntryConverter
import com.wavesenterprise.sdk.node.domain.DataEntry
import com.wavesenterprise.sdk.node.domain.DataKey
import com.wavesenterprise.sdk.node.domain.DataValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.math.BigDecimal
import java.util.stream.Stream

internal class ContractFromDataEntryConverterTest {

    private val converter = JacksonFromDataEntryConverter(jacksonObjectMapper())

    @Test
    fun `should convert simple dto`() {
        val dataEntry = DataEntry(
            key = DataKey("someKey"),
            value = DataValue.StringDataValue(
                """
                { "name": "john", "age": 33 }
                """.trimIndent()
            )
        )

        val result = converter.convert(dataEntry, SimpleDto::class.java)

        result.apply {
            assertEquals(name, "john")
            assertEquals(age, 33)
        }
    }

    @Test
    fun `should convert type reference`() {
        val dataEntry = DataEntry(
            key = DataKey("someKey"),
            value = DataValue.StringDataValue(
                """
                [{ "name": "john", "age": 33 }, { "name": "jane", "age": 33 }]
                """.trimIndent()
            )
        )

        val result = converter.convert(dataEntry, object : TypeReference<List<SimpleDto>>() {})

        result.apply {
            assertEquals(2, result.size)
            assertEquals("john", get(0).name)
            assertEquals("jane", get(1).name)
        }
    }

    @ParameterizedTest
    @MethodSource("integerClasses")
    fun `should convert int or long`(integerClass: Class<*>) {
        val value = 123L
        val dataEntry = DataEntry(
            key = DataKey("someKey"),
            value = DataValue.IntegerDataValue(value)
        )

        val result = converter.convert(dataEntry, integerClass)

        assertEquals(value, result)
    }

    @Test
    fun `should convert string`() {
        val value = "strValue"
        val dataEntry = DataEntry(
            key = DataKey("someKey"),
            value = DataValue.StringDataValue(value)
        )

        val result = converter.convert(dataEntry, String::class.java)

        assertEquals(value, result)
    }

    @Test
    fun `should convert BigDecimal`() {
        val value = "1.44"
        val dataEntry = DataEntry(
            key = DataKey("someKey"),
            value = DataValue.StringDataValue(value)
        )

        val result = converter.convert(dataEntry, BigDecimal::class.java)

        assertEquals(BigDecimal(value), result)
    }

    companion object {
        @JvmStatic
        fun integerClasses(): Stream<Arguments> =
            Stream.of(
                Arguments.of(Long::class.java),
                Arguments.of(Int::class.java),
                Arguments.of(Integer::class.java),
                Arguments.of(java.lang.Long::class.java),
            )
    }

    class SimpleDto(
        val name: String,
        val age: Int,
    )
}
