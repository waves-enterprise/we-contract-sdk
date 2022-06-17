package com.wavesenterprise.sdk.contract.core.state.mapping

import com.wavesenterprise.sdk.contract.api.state.ContractStateWriter
import com.wavesenterprise.sdk.contract.api.state.mapping.KeyMapper
import com.wavesenterprise.sdk.contract.api.state.mapping.WriteMapping

class WriteMappingImpl<T>(
    private val contractStateWriter: ContractStateWriter,
    private val keyMapper: KeyMapper
) : WriteMapping<T> {

    override fun put(key: String, value: T): WriteMapping<T> = this.also {
        contractStateWriter.put(
            keyMapper.doMapping(key),
            requireNotNull(value) { "Couldn't put null on contract state for key = $key" }
        )
    }
}
