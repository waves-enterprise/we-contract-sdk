package com.wavesenterprise.sdk.contract.api.annotation

import java.lang.annotation.Inherited

/**
 * Class annotated with this annotation is used to handle contract creation and calls.
 * Should have methods annotated with ContractInit or ContractAction.
 *
 * @see ContractInit
 * @see ContractAction
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@Inherited
annotation class ContractHandler
