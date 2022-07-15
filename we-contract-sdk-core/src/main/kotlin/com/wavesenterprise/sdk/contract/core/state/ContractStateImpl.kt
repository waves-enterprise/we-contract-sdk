package com.wavesenterprise.sdk.contract.core.state

import com.wavesenterprise.sdk.contract.api.state.ContractState
import com.wavesenterprise.sdk.contract.api.state.ContractStateReader
import com.wavesenterprise.sdk.contract.api.state.ContractToDataValueConverter
import com.wavesenterprise.sdk.contract.api.state.TypeReference
import com.wavesenterprise.sdk.contract.api.state.mapping.Mapping
import com.wavesenterprise.sdk.contract.core.state.mapping.ClassMappingCacheKey
import com.wavesenterprise.sdk.contract.core.state.mapping.MappingCacheKey
import com.wavesenterprise.sdk.contract.core.state.mapping.MappingImpl
import com.wavesenterprise.sdk.contract.core.state.mapping.PrefixedKeyMapper
import com.wavesenterprise.sdk.contract.core.state.mapping.TypeMappingCacheKey
import com.wavesenterprise.sdk.contract.core.state.mapping.WriteMappingImpl
import com.wavesenterprise.sdk.node.domain.DataEntry
import com.wavesenterprise.sdk.node.domain.DataKey

class ContractStateImpl(
    private val contractStateReader: ContractStateReader,
    private val contractToDataValueConverter: ContractToDataValueConverter,
    private val backingMap: MutableMap<String, DataEntry>,
    private val mappingMap: MutableMap<MappingCacheKey, Mapping<*>>,
) : ContractStateReader by contractStateReader, ContractState {

    private val executionResultMap: MutableMap<String, DataEntry> = hashMapOf()

    override fun results(): List<DataEntry> = ImmutableList(executionResultMap.values.toList())

    override fun put(key: String, value: Any): ContractState = this.also {
        require(key.length <= MAX_KEY_LENGTH) {
            "Contract key length should be less than $MAX_KEY_LENGTH. Actual key value was '$key'"
        } // todo maybe move to DataEntry if used only by contract
        DataEntry(
            key = DataKey(key),
            value = contractToDataValueConverter.convert(value)
        ).also {
            executionResultMap[key] = it
            backingMap[key] = it
        }
    }

    override fun <T> getMapping(type: Class<T>, vararg prefix: String): Mapping<T> {
        val cacheKey = ClassMappingCacheKey(clazz = type, prefix = prefix.toList())
        val result = mappingMap[cacheKey]
        return if (result != null) {
            result as Mapping<T>
        } else {
            MappingImpl(
                writeMapping = WriteMappingImpl(
                    contractStateWriter = this,
                    keyMapper = PrefixedKeyMapper(*prefix),
                ),
                readMapping = contractStateReader.getMapping(type, *prefix),
            ).also {
                mappingMap[cacheKey] = it
            }
        }
    }

    override fun <T> getMapping(typeReference: TypeReference<T>, vararg prefix: String): Mapping<T> {
        val cacheKey = TypeMappingCacheKey(reference = typeReference, prefix = prefix.toList())
        val result = mappingMap[cacheKey]
        return if (result != null) {
            result as Mapping<T>
        } else {
            MappingImpl(
                writeMapping = WriteMappingImpl(
                    contractStateWriter = this,
                    keyMapper = PrefixedKeyMapper(*prefix),
                ),
                readMapping = contractStateReader.getMapping(typeReference, *prefix),
            ).also {
                mappingMap[cacheKey] = it
            }
        }
    }
}

internal class ImmutableList<K>(private val inner: List<K>) : List<K> by inner

private const val MAX_KEY_LENGTH = 100
