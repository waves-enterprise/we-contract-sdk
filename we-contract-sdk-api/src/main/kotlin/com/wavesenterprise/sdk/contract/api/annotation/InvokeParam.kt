package com.wavesenterprise.sdk.contract.api.annotation

/**
 * Creates alias for invoke param.
 *
 * If you do not use this annotation all aliases will be created automatically.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class InvokeParam(
    val name: String
)
