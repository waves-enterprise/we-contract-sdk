package com.wavesenterprise.sdk.contract.core.state.mapping

import com.wavesenterprise.sdk.contract.api.state.TypeReference

data class TypeMappingCacheKey(
    val reference: TypeReference<*>,
    var prefix: Array<String>,
) : MappingCacheKey {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TypeMappingCacheKey

        if (reference != other.reference) return false
        if (!prefix.contentEquals(other.prefix)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = reference.hashCode()
        result = 31 * result + prefix.contentHashCode()
        return result
    }
}
