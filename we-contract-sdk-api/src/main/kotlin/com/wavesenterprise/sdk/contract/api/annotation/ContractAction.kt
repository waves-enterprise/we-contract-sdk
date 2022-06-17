package com.wavesenterprise.sdk.contract.api.annotation

import java.lang.annotation.Inherited

/**
 * Marks some method as handler of a contract call
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
@Inherited
annotation class ContractAction(
    val name: String = ""
)
