package com.wavesenterprise.sdk.contract.api.exception

import java.lang.reflect.Method
import kotlin.RuntimeException

class ContractPreValidationException(
    override val message: String?,
    override val cause: Throwable?,
    val method: Method,
    val implementation: Any,
    val args: Array<Any>,
    val contractId: String,
) : RuntimeException()
