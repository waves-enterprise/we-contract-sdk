package com.wavesenterprise.sdk.contract.api.state.mapping

import java.util.Optional

interface ReadMapping<T> {

    /**
     * Method that find value by key with mapping
     *
     * @param key key to find
     * @return value that was founded or throw exception if did not find it
     */
    operator fun get(key: String): T

    /**
     * Method that try to find value by key with mapping
     *
     * @param key key to find
     * @return Optional with result of search
     */
    fun tryGet(key: String): Optional<T>

    /**
     * Find values by set of id
     *
     * @param ids
     * @return map of ids to found values.
     */
    fun getAll(ids: Set<String>): Map<String, T>

    /**
     * Checks if a value with given key exists in mapping
     *
     * @param key
     * @return true if value exists
     */
    fun has(key: String): Boolean = tryGet(key).isPresent

    /**
     * Checks all values are present in mapping
     *
     * @param key
     * @return true if value exists
     */
    fun hasAll(keys: Set<String>): Boolean = getAll(keys).run { entries.size == keys.distinct().size }
}
