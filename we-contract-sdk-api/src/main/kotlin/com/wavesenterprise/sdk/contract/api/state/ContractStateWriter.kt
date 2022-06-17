package com.wavesenterprise.sdk.contract.api.state

import com.wavesenterprise.sdk.contract.api.state.mapping.Mapping
import com.wavesenterprise.sdk.node.domain.DataEntry

interface ContractStateWriter {

    /**
     * Saves key-value pair to state
     *
     */
    fun put(key: String, value: Any): ContractState

    /**
     * Get written results
     */
    fun results(): List<DataEntry>

    /**
     *  Constructs mapping of keys having values with specified type and prefix
     *
     * @param type type of objects
     * @param prefix prefix parts joined by ContractKeys.PREFIX_KEY_DELIMITER
     */
    fun <T> getMapping(type: Class<T>, vararg prefix: String): Mapping<T>

    /**
     *  Constructs mapping of keys having values with specified type and prefix
     *
     * @param typeReference typeReference of objects
     * @param prefix prefix parts joined by ContractKeys.PREFIX_KEY_DELIMITER
     */
    fun <T> getMapping(typeReference: TypeReference<T>, vararg prefix: String): Mapping<T>
}
