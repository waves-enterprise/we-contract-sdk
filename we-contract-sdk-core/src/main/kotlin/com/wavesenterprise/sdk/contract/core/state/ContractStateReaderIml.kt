package com.wavesenterprise.sdk.contract.core.state

import com.wavesenterprise.sdk.contract.api.state.ContractFromDataEntryConverter
import com.wavesenterprise.sdk.contract.api.state.ContractStateReader
import com.wavesenterprise.sdk.contract.api.state.NodeContractStateValuesProvider
import com.wavesenterprise.sdk.contract.api.state.TypeReference
import com.wavesenterprise.sdk.contract.api.state.getForKeyAsNullable
import com.wavesenterprise.sdk.contract.api.state.mapping.ReadMapping
import com.wavesenterprise.sdk.contract.api.wrc.WRC12Meta
import com.wavesenterprise.sdk.contract.core.state.mapping.PrefixedKeyMapper
import com.wavesenterprise.sdk.contract.core.state.mapping.ReadMappingForType
import com.wavesenterprise.sdk.contract.core.state.mapping.ReadMappingForTypeReference
import com.wavesenterprise.sdk.node.domain.DataEntry
import com.wavesenterprise.sdk.node.domain.contract.ContractId
import java.util.Optional

class ContractStateReaderIml(
    private val contractId: ContractId,
    private val nodeContractStateValuesProvider: NodeContractStateValuesProvider,
    private val contractFromDataEntryConverter: ContractFromDataEntryConverter,
    private val backingMap: MutableMap<String, DataEntry> = hashMapOf(),
) : ContractStateReader {

    override fun <T> get(key: String, valueType: Class<T>): T =
        requireNotNull(getNullableValue(key, valueType)) { errorMessage(key) }

    override fun <T> get(key: String, typeReference: TypeReference<T>): T =
        requireNotNull(getNullableValue(key, typeReference)) { errorMessage(key) }

    override fun <T> tryGet(key: String, valueType: Class<T>): Optional<T> =
        Optional.ofNullable(getNullableValue(key, valueType))

    override fun <T> tryGet(key: String, typeReference: TypeReference<T>): Optional<T> =
        Optional.ofNullable(getNullableValue(key, typeReference))

    override fun getBoolean(key: String): Boolean =
        requireNotNull(getNullableValue(key, Boolean::class.java)) { errorMessage(key) }

    override fun tryGetBoolean(key: String): Optional<Boolean> =
        Optional.ofNullable(getNullableValue(key, Boolean::class.java))

    override fun getLong(key: String): Long =
        requireNotNull(getNullableValue(key, Long::class.java)) { errorMessage(key) }

    override fun tryGetInteger(key: String): Optional<Long> =
        Optional.ofNullable(getNullableValue(key, Long::class.java))

    override fun <T> getAll(keys: Set<String>, valueType: Class<T>): Map<String, T> {
        return getAllWithEntryMapping(keys) {
            contractFromDataEntryConverter.convert(it, valueType)
        }
    }

    override fun <T> getAll(keys: Set<String>, typeReference: TypeReference<T>): Map<String, T> {
        return getAllWithEntryMapping(keys) {
            contractFromDataEntryConverter.convert(it, typeReference)
        }
    }

    override fun <T> getMapping(type: Class<T>, vararg prefix: String): ReadMapping<T> =
        ReadMappingForType(
            contractStateReader = this,
            keyMapper = PrefixedKeyMapper(*prefix),
            type = type
        )

    override fun <T> getMapping(typeReference: TypeReference<T>, vararg prefix: String): ReadMapping<T> =
        ReadMappingForTypeReference(
            contractStateReader = this,
            keyMapper = PrefixedKeyMapper(*prefix),
            typeReference = typeReference
        )

    override fun getContractMeta(): Optional<WRC12Meta> {
        TODO("Not yet implemented")
    }

    private fun <T> getAllWithEntryMapping(
        keys: Set<String>,
        entryMappingFn: (DataEntry) -> T
    ): Map<String, T> {
        val keysToBeRequested = keys.minus(backingMap.keys)
        if (keysToBeRequested.isNotEmpty()) {
            backingMap.putAll(
                nodeContractStateValuesProvider.getForKeys(contractId, keysToBeRequested).map { it.key.value to it }
            )
        }
        return backingMap.entries
            .filter { keys.contains(it.key) }
            .associate { it.key to entryMappingFn(it.value) }
    }

    private fun getKeyValue(key: String): DataEntry? =
        backingMap[key] ?: nodeContractStateValuesProvider.getForKeyAsNullable(contractId, key)?.also { backingMap[key] = it }

    private fun <T> getNullableValue(key: String, valueType: Class<T>): T? =
        getKeyValue(key)?.run {
            contractFromDataEntryConverter.convert(this, valueType)
        }

    private fun <T> getNullableValue(key: String, typeReference: TypeReference<T>): T? =
        getKeyValue(key)?.run {
            contractFromDataEntryConverter.convert(this, typeReference)
        }

    private fun errorMessage(key: String) =
        "No value found for key $key on contract state with id = ${contractId.asBase58String()}"
}
