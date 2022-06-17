package com.wavesenterprise.sdk.contract.api.annotation

import java.lang.annotation.Inherited

/**
 * Marks method as a handler for contract creation
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
@Inherited
annotation class ContractInit(
    val name: String = ""
)
