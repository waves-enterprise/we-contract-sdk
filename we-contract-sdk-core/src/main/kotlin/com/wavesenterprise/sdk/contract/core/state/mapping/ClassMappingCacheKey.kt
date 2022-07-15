package com.wavesenterprise.sdk.contract.core.state.mapping

data class ClassMappingCacheKey(
    val clazz: Class<*>,
    var prefix: List<String>,
) : MappingCacheKey
