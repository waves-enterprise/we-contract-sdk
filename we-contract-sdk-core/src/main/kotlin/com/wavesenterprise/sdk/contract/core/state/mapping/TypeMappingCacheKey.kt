package com.wavesenterprise.sdk.contract.core.state.mapping

import com.wavesenterprise.sdk.contract.api.state.TypeReference

data class TypeMappingCacheKey(
    val reference: TypeReference<*>,
    var prefix: List<String>,
) : MappingCacheKey
