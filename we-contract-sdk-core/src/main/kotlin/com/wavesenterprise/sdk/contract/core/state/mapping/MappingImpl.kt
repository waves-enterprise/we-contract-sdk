package com.wavesenterprise.sdk.contract.core.state.mapping

import com.wavesenterprise.sdk.contract.api.state.mapping.Mapping
import com.wavesenterprise.sdk.contract.api.state.mapping.ReadMapping
import com.wavesenterprise.sdk.contract.api.state.mapping.WriteMapping

class MappingImpl<T>(
    private val writeMapping: WriteMapping<T>,
    private val readMapping: ReadMapping<T>,
) : WriteMapping<T> by writeMapping,
    ReadMapping<T> by readMapping,
    Mapping<T>
