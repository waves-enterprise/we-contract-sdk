package com.wavesenterprise.sdk.contract.core.state

import com.google.common.base.CaseFormat.LOWER_CAMEL
import com.google.common.base.CaseFormat.UPPER_UNDERSCORE
import com.google.common.primitives.Primitives
import com.wavesenterprise.sdk.contract.api.state.ContractState
import com.wavesenterprise.sdk.contract.api.state.TypeReference
import com.wavesenterprise.sdk.contract.api.state.mapping.Mapping
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.Optional
import kotlin.reflect.KProperty

/**
 * An extension function that allows access to mapped value in
 * map-like fashion
 */
inline operator fun <reified T> Mapping<T>.set(key: String, value: T) {
    put(key, value)
}

/**
 * An extension function that allows delegating reading variables
 * to state with 'by' operator in map-like fashion
 */
inline operator fun <reified T> ContractState.getValue(
    thisRef: Any?,
    property: KProperty<*>
): T {
    val type = object : TypeReference<T>() {}.getType()
    val name = keyName(property.name)
    if (type.isMapping()) {
        require(!property.returnType.isMarkedNullable) {
            "Can not declare nullable mapping"
        }
        val mappingType = (type as ParameterizedType).actualTypeArguments[0]
        return if (mappingType.isSimpleType()) {
            getMapping(mappingType as Class<*>, name) as T
        } else {
            val typeRef = DynamicTypeReference(mappingType)
            getMapping(typeRef, name) as T
        }
    } else {
        if (type.isSimpleType()) {
            return if (property.returnType.isMarkedNullable) {
                val opt = tryGet(name, type as Class<*>) as Optional<*>
                opt.orElse(null) as T
            } else {
                get(name, type as Class<*>) as T
            }
        } else {
            return if (property.returnType.isMarkedNullable) {
                val typeRef = DynamicTypeReference(type)
                val opt = tryGet(name, typeRef) as Optional<*>
                opt.orElse(null) as T
            } else {
                val typeRef = DynamicTypeReference(type)
                get(name, typeRef) as T
            }
        }
    }
}

/**
 * An extension function that allows delegating writing variables
 * to state with 'by' operator in map-like fashion
 */
inline operator fun <reified T> ContractState.setValue(
    thisRef: Any?,
    property: KProperty<*>,
    value: T
) {
    put(keyName(property.name), value as Any)
}

fun keyName(name: String): String {
    return checkNotNull(LOWER_CAMEL.converterTo(UPPER_UNDERSCORE).convert(name))
}

fun Type.isMapping() =
    when (this) {
        is ParameterizedType -> (rawType as Class<*>).name == Mapping::class.java.name
        else -> false
    }

fun Type.isSimpleType() =
    when (this) {
        is Class<*> -> this == String::class.java || Primitives.isWrapperType(this)
        else -> false
    }

class DynamicTypeReference(
    private val type: Type
) : TypeReference<Any>() {
    override fun getType(): Type {
        return type
    }

    override fun toString(): String = "DynamicTypeReference[$type]"
}
