package com.wavesenterprise.sdk.contract.core.state.mapping

import com.wavesenterprise.sdk.contract.api.domain.ContractKeys.PREFIX_KEY_DELIMITER
import com.wavesenterprise.sdk.contract.api.state.mapping.KeyMapper

class PrefixedKeyMapper(
    prefixes: Set<String>
) : KeyMapper {

    private val prefixForKey: String

    init {
        prefixForKey = prefixes.joinToString(PREFIX_KEY_DELIMITER)
            .let { if (it.isEmpty()) it else it + PREFIX_KEY_DELIMITER }
    }

    constructor(vararg prefixes: String) : this(prefixes.toSet())

    override fun doMapping(key: String): String = prefixForKey + key

    override fun parseMapping(mappingKey: String): String =
        mappingKey.substringAfter(prefixForKey)
}
