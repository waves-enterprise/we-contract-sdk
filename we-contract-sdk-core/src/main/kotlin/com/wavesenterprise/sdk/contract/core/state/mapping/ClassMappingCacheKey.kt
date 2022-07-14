package com.wavesenterprise.sdk.contract.core.state.mapping

data class ClassMappingCacheKey(
    val clazz: Class<*>,
    var prefix: Array<String>,
) : MappingCacheKey {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClassMappingCacheKey

        if (clazz != other.clazz) return false
        if (!prefix.contentEquals(other.prefix)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = clazz.hashCode()
        result = 31 * result + prefix.contentHashCode()
        return result
    }
}
