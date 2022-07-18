package com.wavesenterprise.sdk.contract.core.state.mapping

import com.wavesenterprise.sdk.contract.api.state.TypeReference

sealed interface MappingCacheKey {
    val prefix: List<String>
}
data class TypeMappingCacheKey(
    val reference: TypeReference<*>,
    override val prefix: List<String>,
) : MappingCacheKey

data class ClassMappingCacheKey(
    val clazz: Class<*>,
    override val prefix: List<String>,
) : MappingCacheKey
