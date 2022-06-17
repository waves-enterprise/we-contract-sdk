package com.wavesenterprise.sdk.contract.api.state

import com.wavesenterprise.sdk.contract.api.state.mapping.ReadMapping
import com.wavesenterprise.sdk.contract.api.wrc.WRC12Meta
import java.util.Optional

interface ContractStateReader {
    /**
     * Gets value by key
     *
     * @param key key to find
     * @param valueType type of returned value
     * @return return found value or throw ValidateException if value is null
     */
    operator fun <T> get(key: String, valueType: Class<T>): T

    /**
     * Finds value by key
     *
     * @param key key to find
     * @param valueType type of returned value
     * @return Optional
     */
    fun <T> tryGet(key: String, valueType: Class<T>): Optional<T>

    /**
     * Finds value by key
     *
     * @param key key to find
     * @param typeReference typeReference of returned value
     * @return return found value or throw ValidateException if value is null
     */
    operator fun <T> get(key: String, typeReference: TypeReference<T>): T

    /**
     * Finds value by key
     *
     * @param key key to find
     * @param typeReference typeReference of returned value
     * @return result found or null wrapped by Optional
     */
    fun <T> tryGet(key: String, typeReference: TypeReference<T>): Optional<T>

    /**
     * Finds Boolean value by key
     *
     * @param key key to find
     * @return return found value or throw ValidateException if value is null
     */
    fun getBoolean(key: String): Boolean

    /**
     * Finds value by key
     *
     * @param key key to find
     * @return result found or null wrapped by Optional
     */
    fun tryGetBoolean(key: String): Optional<Boolean>

    /**
     * Finds integer value by key
     *
     * @param key key to find
     * @return return found value or throw ValidateException if value is null
     */
    fun getLong(key: String): Long

    /**
     * Finds integer value by key
     *
     * @param key key to find
     * @return result found or null wrapped by Optional
     */
    fun tryGetInteger(key: String): Optional<Long>

    /**
     * Get a map where contract keys are mapped to their typed values
     *
     * @param keys keys to be found
     * @param valueType type of the value
     * @return map of keys to values
     */
    fun <T> getAll(keys: Set<String>, valueType: Class<T>): Map<String, T>

    /**
     * Get a map where contract keys are mapped to their typed values
     *
     * @param keys keys to be found
     * @param typeReference typeReference of the value
     * @return map of keys to values
     */
    fun <T> getAll(keys: Set<String>, typeReference: TypeReference<T>): Map<String, T>

    /**
     * Constructs READ ONLY mapping for type and prefix
     * @param type type of values in the mapping
     * @param prefix prefix of the mapping
     * @return ReadMapping for type
     </T> */
    fun <T> getMapping(type: Class<T>, vararg prefix: String): ReadMapping<T>

    /**
     * Constructs READ ONLY mapping for TypeReference and prefix
     * @param typeReference typeReference of values in the mapping
     * @param prefix prefix of the mapping
     * @return ReadMapping for typeReference
     </T> */
    fun <T> getMapping(typeReference: TypeReference<T>, vararg prefix: String): ReadMapping<T>

    /**
     * Get contract WRC12Meta if present
     */
    fun getContractMeta(): Optional<WRC12Meta>
}
