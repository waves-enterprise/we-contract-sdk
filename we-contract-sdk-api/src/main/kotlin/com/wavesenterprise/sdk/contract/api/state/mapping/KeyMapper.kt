package com.wavesenterprise.sdk.contract.api.state.mapping

interface KeyMapper {
    fun doMapping(key: String): String
    fun parseMapping(mappingKey: String): String
}
