package com.wavesenterprise.sdk.contract.core.state

import com.wavesenterprise.sdk.contract.api.state.ContractFromDataEntryConverter
import com.wavesenterprise.sdk.contract.api.state.ContractState
import com.wavesenterprise.sdk.contract.api.state.ContractToDataValueConverter
import com.wavesenterprise.sdk.contract.api.state.NodeContractStateValuesProvider
import com.wavesenterprise.sdk.contract.api.state.TypeReference
import com.wavesenterprise.sdk.contract.api.state.mapping.Mapping
import com.wavesenterprise.sdk.contract.core.state.mapping.MappingCacheKey
import com.wavesenterprise.sdk.node.domain.DataEntry
import com.wavesenterprise.sdk.node.domain.DataKey
import com.wavesenterprise.sdk.node.domain.DataValue
import com.wavesenterprise.sdk.node.domain.contract.ContractId
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Optional

@ExtendWith(MockKExtension::class)
internal class ContractStateTest {

    @MockK
    lateinit var nodeContractStateValuesProvider: NodeContractStateValuesProvider

    @MockK
    lateinit var contractFromDataEntryConverter: ContractFromDataEntryConverter

    @MockK
    lateinit var contractToDataValueConverter: ContractToDataValueConverter

    private val mappingMapForState: MutableMap<MappingCacheKey, Mapping<*>> = spyk(hashMapOf())

    private lateinit var contractState: ContractState

    private val backingMapForState: MutableMap<String, DataEntry> = hashMapOf()

    @BeforeEach
    fun prepare() {
        val contractId = ContractId.fromByteArray(ByteArray(12) { it.toByte() })
        val backingMapForState: MutableMap<String, DataEntry> = backingMapForState
        val contractStateReader = ContractStateReaderIml(
            contractId = contractId,
            nodeContractStateValuesProvider = nodeContractStateValuesProvider,
            contractFromDataEntryConverter = contractFromDataEntryConverter,
            backingMap = backingMapForState,
        )
        contractState = ContractStateImpl(
            contractStateReader = contractStateReader,
            contractToDataValueConverter = contractToDataValueConverter,
            backingMap = backingMapForState,
            mappingMap = mappingMapForState,
        )
    }

    @Test
    fun `should write and read state for custom domain object having it in results`() {
        val someDomainObj = SomeDomainObject()
        every {
            contractToDataValueConverter.convert(someDomainObj)
        } returns DataValue.StringDataValue(someDomainObj.toString())
        every {
            contractFromDataEntryConverter.convert(any(), SomeDomainObject::class.java)
        } returns SomeDomainObject()

        val key = "ABCD_KEY"
        val value = SomeDomainObject()
        contractState.put(key, value)
        val someDomainObject = contractState[key, SomeDomainObject::class.java]

        assertEquals(value, someDomainObject)
        assertEquals(
            value.toString(),
            (contractState.results().find { it.key.value == key }?.value as DataValue.StringDataValue).value
        )
        verify {
            contractToDataValueConverter.convert(someDomainObj)
            contractFromDataEntryConverter.convert(any(), SomeDomainObject::class.java)
        }
    }

    @Test
    fun `state should proxy read to nodeContractStateProvider and results should contain only written values`() {
        val writeKey = "WRITE_KEY"
        val readKey = "READ_KEY"
        val stateValue = "bla-bla-from-state"
        val writtenValue = "written Value"
        every {
            nodeContractStateValuesProvider.getForKey(any(), readKey)
        } returns Optional.of(
            DataEntry(
                DataKey(readKey),
                DataValue.StringDataValue.fromString(stateValue)
            )
        )
        every {
            contractToDataValueConverter.convert<String>(any())
        } answers {
            DataValue.StringDataValue(firstArg())
        }
        every {
            contractFromDataEntryConverter.convert(any(), String::class.java)
        } answers {
            ((firstArg() as DataEntry).value as DataValue.StringDataValue).value
        }

        val readValueFromState = contractState[readKey, String::class.java]
        contractState.put(writeKey, writtenValue)
        val actualWrittenValue = contractState[writeKey, String::class.java]

        assertEquals(stateValue, readValueFromState)
        assertEquals(writtenValue, actualWrittenValue)
        verify {
            nodeContractStateValuesProvider.getForKey(any(), readKey)
        }
        backingMapForState.apply {
            assertEquals(2, size)
            assertTrue(containsKey(writeKey))
            assertTrue(containsKey(readKey))
        }
        contractState.results().apply {
            assertEquals(1, size)
            assertTrue(map { it.key.value }.contains(writeKey))
        }
    }

    @Test
    fun `should read and write mapping`() {
        val mappingPrefix = "MY_DOMAIN_OBJECT"
        val objectId = "someDomainObjectId"
        val someDomainObj = SomeDomainObject()
        every {
            contractToDataValueConverter.convert(someDomainObj)
        } returns DataValue.StringDataValue(someDomainObj.toString())
        every {
            contractFromDataEntryConverter.convert(any(), SomeDomainObject::class.java)
        } returns SomeDomainObject()

        val mapping = contractState.getMapping(SomeDomainObject::class.java, mappingPrefix)
        mapping.put(objectId, SomeDomainObject())
        val domainObjectFromMapping = mapping[objectId]
        val domainObjectRetrievedByKey =
            contractState[mappingPrefix + "_" + objectId, SomeDomainObject::class.java]

        assertEquals(someDomainObj, domainObjectFromMapping)
        assertEquals(someDomainObj, domainObjectRetrievedByKey)
        verify {
            contractToDataValueConverter.convert(someDomainObj)
            contractFromDataEntryConverter.convert(any(), SomeDomainObject::class.java)
        }
    }

    @Test
    fun `should use cache of mapping map with key as Class`() {
        val mappingPrefix = "MY_DOMAIN_OBJECT"
        val callGetMappingCount = 2

        repeat(callGetMappingCount) {
            contractState.getMapping(SomeDomainObject::class.java, mappingPrefix)
        }

        verify { mappingMapForState[any()] }
    }

    @Test
    fun `should use cache of mapping map with key as TypeReference`() {
        val mappingPrefix = "MY_DOMAIN_OBJECT"
        val callGetMappingCount = 2

        repeat(callGetMappingCount) {
            contractState.getMapping(object : TypeReference<SomeDomainObject>() {}, mappingPrefix)
        }

        verify { mappingMapForState[any()] }
    }
}

data class SomeDomainObject(
    val name: String = "name",
    val count: Int = 10,
    val description: String = "someDescr",
)
