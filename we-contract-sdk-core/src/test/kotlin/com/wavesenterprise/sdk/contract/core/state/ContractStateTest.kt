package com.wavesenterprise.sdk.contract.core.state

import com.wavesenterprise.sdk.contract.api.state.ContractFromDataEntryConverter
import com.wavesenterprise.sdk.contract.api.state.ContractState
import com.wavesenterprise.sdk.contract.api.state.ContractToDataValueConverter
import com.wavesenterprise.sdk.contract.api.state.NodeContractStateValuesProvider
import com.wavesenterprise.sdk.contract.api.state.TypeReference
import com.wavesenterprise.sdk.contract.api.state.mapping.Mapping
import com.wavesenterprise.sdk.contract.core.state.factory.ExternalContractStateFactory
import com.wavesenterprise.sdk.contract.core.state.mapping.MappingCacheKey
import com.wavesenterprise.sdk.contract.core.utils.contractId
import com.wavesenterprise.sdk.contract.core.utils.dataEntry
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
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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
        val contractStateReaderFactory = ExternalContractStateFactory(
            contractFromDataEntryConverter = contractFromDataEntryConverter,
            nodeContractStateValuesProvider = nodeContractStateValuesProvider,
        )
        contractState = ContractStateImpl(
            contractStateReader = contractStateReader,
            contractToDataValueConverter = contractToDataValueConverter,
            backingMap = backingMapForState,
            mappingMap = mappingMapForState,
            contractStateReaderFactory = contractStateReaderFactory,
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
    fun `should throw exception when length of key more than limit`() {
        val longKey = "k".repeat(MAX_KEY_LENGTH + 1)
        assertThrows<IllegalArgumentException> {
            contractState.put(longKey, SomeDomainObject())
        }.apply {
            assertEquals(
                "Contract key length should be less than $MAX_KEY_LENGTH. Actual key value was '$longKey'",
                message
            )
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

    @Test
    fun `should check hasAll for mapping`() {
        val mappingPrefix = "MY_DOMAIN_OBJECT"
        val objectId = "someDomainObjectId"
        val otherObjectId = "someOtherDomainObjectId"
        val notFoundObjectId = "anotherOneNotFoundObjectId"
        val someDomainObj = SomeDomainObject()
        val someOtherDomainObj = SomeDomainObject().copy(name = "other")
        every {
            contractToDataValueConverter.convert(someDomainObj)
        } returns DataValue.StringDataValue(someDomainObj.toString())
        every {
            contractToDataValueConverter.convert(someOtherDomainObj)
        } returns DataValue.StringDataValue(someOtherDomainObj.toString())
        every {
            contractFromDataEntryConverter.convert(any(), SomeDomainObject::class.java)
        } returns SomeDomainObject()
        every {
            nodeContractStateValuesProvider.getForKeys(any(), setOf(mappingPrefix + "_" + notFoundObjectId))
        } returns emptyList()

        val mapping = contractState.getMapping(SomeDomainObject::class.java, mappingPrefix)
        mapping.put(objectId, someDomainObj)
        mapping.put(otherObjectId, someOtherDomainObj)

        assertTrue(mapping.hasAll(setOf(objectId, otherObjectId)))
        assertFalse(mapping.hasAll(setOf(objectId, otherObjectId, notFoundObjectId)))
    }

    @Test
    fun `should return correct state reader`() {
        val someDomainObject = SomeDomainObject()
        val contractId = contractId()
        val key = "key"
        val dataEntry = dataEntry()
        every {
            nodeContractStateValuesProvider.getForKey(contractId, key)
        } returns Optional.of(dataEntry)
        every {
            contractFromDataEntryConverter.convert(dataEntry, SomeDomainObject::class.java)
        } returns someDomainObject

        val contractStateReader = contractState.external(contractId = contractId)
        contractStateReader.tryGet(key, SomeDomainObject::class.java)

        verify { nodeContractStateValuesProvider.getForKey(contractId, key) }
        verify { contractFromDataEntryConverter.convert(dataEntry, SomeDomainObject::class.java) }
    }

    companion object {
        const val MAX_KEY_LENGTH = 100
    }
}

data class SomeDomainObject(
    val name: String = "name",
    val count: Int = 10,
    val description: String = "someDescr",
)
