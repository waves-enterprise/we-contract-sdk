package com.wavesenterprise.sdk.contract.client.invocation

import com.wavesenterprise.sdk.contract.api.annotation.ContractAction
import com.wavesenterprise.sdk.contract.api.annotation.ContractHandler
import com.wavesenterprise.sdk.contract.api.annotation.ContractInit
import com.wavesenterprise.sdk.contract.api.annotation.InvokeParam

@ContractHandler
class TestProcessor {

    @ContractInit
    fun testActionInit(@InvokeParam(name = REQUEST_PARAM_KEY) request: TestObject) { }

    @ContractAction
    fun testActionCallBoolean(@InvokeParam(name = REQUEST_PARAM_KEY) request: Boolean) { }

    @ContractAction
    fun testActionCallString(@InvokeParam(name = REQUEST_PARAM_KEY) request: String) { }

    @ContractAction
    fun testActionCallInteger(@InvokeParam(name = REQUEST_PARAM_KEY) request: Int) { }

    @ContractAction
    fun testActionCallWithoutParams() { }

    @ContractAction
    fun testActionCallWithSeveralParameters(
        @InvokeParam(name = "obj") obj: TestObject,
        @InvokeParam(name = "int") int: Int,
        @InvokeParam(name = "str") str: String,
        @InvokeParam(name = "bool") bool: Boolean,
    ) { }

    companion object {
        const val REQUEST_PARAM_KEY = "request"
    }
}
