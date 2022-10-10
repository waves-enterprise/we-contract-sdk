package com.wavesenterprise.sdk.contract.core.state

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.wavesenterprise.sdk.contract.api.state.ContractState
import com.wavesenterprise.sdk.contract.api.state.NodeContractStateValuesProvider
import com.wavesenterprise.sdk.contract.api.state.mapping.Mapping
import com.wavesenterprise.sdk.contract.core.state.factory.DefaultBackingMapContractStateFactory
import com.wavesenterprise.sdk.contract.jackson.JacksonContractToDataValueConverter
import com.wavesenterprise.sdk.contract.jackson.JacksonFromDataEntryConverter
import com.wavesenterprise.sdk.contract.test.data.TestDataProvider
import com.wavesenterprise.sdk.node.domain.DataEntry
import com.wavesenterprise.sdk.node.domain.DataKey
import com.wavesenterprise.sdk.node.domain.DataValue
import com.wavesenterprise.sdk.node.domain.contract.ContractId
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.math.BigInteger
import java.util.Optional

internal class ContractStateExTest {

    private val values = mutableMapOf<String, Any>()

    private val objectMapper = jacksonObjectMapper()

    @Test
    fun `should delegate READ to simple read only property`() {
        values["X"] = "x_value"
        val x: String by makeState()

        assertEquals("x_value", x)
    }

    @Test
    fun `should delegate READ to big int property`() {
        values["X"] = "123"
        val x: BigInteger by makeState()

        assertEquals(BigInteger("123"), x)
    }

    @Test
    fun `should delegate READ to simple int property`() {
        values["X"] = 1
        val x: Int by makeState()

        assertEquals(1, x)
    }

    @Test
    fun `should delegate READ to simple long property`() {
        values["X"] = 1
        val x: Long by makeState()

        assertEquals(1, x)
    }

    @Test
    fun `should delegate READ to simple read only property with case conversion`() {
        values["MY_KEY"] = "x_value"
        val myKey: String by makeState()

        assertEquals("x_value", myKey)
    }

    @Test
    fun `should support READ nullable properties`() {
        values["MY_KEY"] = "x_value"
        val myOtherKey: String? by makeState()

        assertNull(myOtherKey)
    }

    @Test
    fun `should support READ complex types`() {
        values["MY_KEY"] = objectMapper.writeValueAsString(User("test"))
        val myKey: User by makeState()

        assertEquals(myKey, User("test"))
    }

    @Test
    fun `should support READ nullable complex types`() {
        values["MY_KEY"] = objectMapper.writeValueAsString(User("test"))
        val myOtherKey: User? by makeState()

        assertNull(myOtherKey)
    }

    @Test
    fun `should support READ generic complex types`() {
        values["MY_KEY"] = objectMapper.writeValueAsString(listOf(User("test")))
        val myKey: List<User> by makeState()

        assertEquals(myKey, listOf(User("test")))
    }

    @Test
    fun `should delegate WRITE to simple property`() {
        val state = makeState()
        var x: String? by state
        x = "x_value"
        val entryValue = state.result("X")

        assertEquals("x_value", (entryValue.value as DataValue.StringDataValue).value)
    }

    @Test
    fun `should delegate WRITE complex type`() {
        val state = makeState()
        var x: User? by state
        x = User("test")
        val entry = state.result("X")

        assertEquals(objectMapper.writeValueAsString(User("test")), (entry.value as DataValue.StringDataValue).value)
    }

    @Test
    fun `should support READ simple type from mapping`() {
        values["MAPPING_X"] = "x_value"
        val mapping: Mapping<String> by makeState()

        assertEquals(mapping["X"], "x_value")
    }

    @Test
    fun `should support READ int type from mapping`() {
        values["MAPPING_X"] = 3
        val mapping: Mapping<Int> by makeState()

        assertEquals(mapping["X"], 3)
    }

    @Test
    fun `should support READ big int type from mapping`() {
        values["MAPPING_X"] = "1"
        val mapping: Mapping<BigInteger> by makeState()

        assertEquals(mapping["X"], BigInteger("1"))
    }

    @Test
    fun `should support READ complex type from mapping`() {
        values["MAPPING_X"] = objectMapper.writeValueAsString(User("test"))
        val mapping: Mapping<User> by makeState()

        assertEquals(mapping["X"], User("test"))
    }

    @Test
    fun `should support READ generic complex type from mapping`() {
        values["MAPPING_X"] = objectMapper.writeValueAsString(listOf(User("test")))
        val mapping: Mapping<List<User>> by makeState()

        assertEquals(mapping["X"], listOf(User("test")))
    }

    @Test
    fun `should support WRITE simple type to mapping`() {
        val state = makeState()
        val mapping: Mapping<String> by state
        mapping["X"] = "Z"
        val entry = state.result("MAPPING_X")

        assertEquals("Z", (entry.value as DataValue.StringDataValue).value)
    }

    @Test
    fun `should support WRITE complex type to mapping`() {
        val state = makeState()
        val mapping: Mapping<User> by state
        mapping["X"] = User("test")
        val entry = state.result("MAPPING_X")

        assertEquals(objectMapper.writeValueAsString(User("test")), (entry.value as DataValue.StringDataValue).value)
    }

    @Test
    fun `should be callable from contract instance`() {
        val state = makeState()
        val example = ExampleImpl(state)
        example.test()
        val entry = state.result("X")

        assertEquals("Z", (entry.value as DataValue.StringDataValue).value)
    }

    private fun makeState(): ContractState {
        val nodeContractStateValuesProvider = mockk<NodeContractStateValuesProvider>()
        val converter = JacksonContractToDataValueConverter(objectMapper)
        every { nodeContractStateValuesProvider.getForKey(any(), any()) } answers {
            val key = it.invocation.args[1] as String
            values[key].let { it1 ->
                Optional.ofNullable(
                    if (it1 != null) {
                        DataEntry(
                            key = DataKey(key),
                            value = converter.convert(it1),
                        )
                    } else null
                )
            }
        }
        return DefaultBackingMapContractStateFactory(
            nodeContractStateValuesProvider = nodeContractStateValuesProvider,
            contractToDataValueConverter = JacksonContractToDataValueConverter(objectMapper),
            contractFromDataEntryConverter = JacksonFromDataEntryConverter(objectMapper)
        ).buildContractState(ContractId(TestDataProvider.txId()))
    }

    private fun ContractState.result(key: String) = results()[0]

    data class User(
        val name: String
    )
}

class ExampleImpl(
    state: ContractState,
) {
    var x: String by state

    fun test() {
        x = "Z"
    }
}
