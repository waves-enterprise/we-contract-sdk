package com.wavesenterprise.sdk.contract.api.state.mapping

/**
 * Manage contract key mappings and WRITE them
 */
interface WriteMapping<T> {

    /**
     * Method that save key-value pair with key-mapping
     */
    fun put(key: String, value: T): WriteMapping<T>
}
