package com.wavesenterprise.sdk.contract.api.state

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

abstract class TypeReference<T>() {

    private val storedType: Type

    init {
        val superClass = javaClass.genericSuperclass
        require(superClass !is Class<*>) { // sanity check, should never happen
            "Internal error: TypeReference constructed without actual type information"
        }
        storedType = (superClass as ParameterizedType).actualTypeArguments[0]
    }

    open fun getType(): Type {
        return storedType
    }
}
