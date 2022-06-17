package com.wavesenterprise.sdk.contract.core.state.mapping

import com.wavesenterprise.sdk.contract.api.state.ContractStateReader
import com.wavesenterprise.sdk.contract.api.state.TypeReference
import com.wavesenterprise.sdk.contract.api.state.mapping.KeyMapper
import com.wavesenterprise.sdk.contract.api.state.mapping.ReadMapping
import java.util.Optional

class ReadMappingForTypeReference<T>(
    private val contractStateReader: ContractStateReader,
    private val keyMapper: KeyMapper,
    private val typeReference: TypeReference<T>,
) : ReadMapping<T> {

    override fun get(key: String): T = contractStateReader[keyMapper.doMapping(key), typeReference]

    override fun tryGet(key: String): Optional<T> = contractStateReader.tryGet(keyMapper.doMapping(key), typeReference)

    override fun getAll(ids: Set<String>): Map<String, T> = contractStateReader.getAll(mapKeys(ids), typeReference)

    private fun mapKeys(ids: Set<String>) = ids.map { keyMapper.doMapping(it) }.toSet()
}
