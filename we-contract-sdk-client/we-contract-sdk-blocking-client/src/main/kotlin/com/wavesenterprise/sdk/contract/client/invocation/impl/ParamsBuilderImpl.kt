package com.wavesenterprise.sdk.contract.client.invocation.impl

import com.wavesenterprise.sdk.contract.api.annotation.InvokeParam
import com.wavesenterprise.sdk.contract.api.state.ContractToDataValueConverter
import com.wavesenterprise.sdk.contract.client.invocation.ParamsBuilder
import com.wavesenterprise.sdk.node.domain.DataEntry
import com.wavesenterprise.sdk.node.domain.DataKey
import java.lang.reflect.Method
import java.lang.reflect.Parameter

class ParamsBuilderImpl(
    private val converter: ContractToDataValueConverter,
) : ParamsBuilder {

    override fun build(method: Method, args: Array<out Any>): List<DataEntry> {
        val actionName = method.name
        val invokeParams = buildList {
            add(
                index = 0,
                element = DataEntry(
                    key = DataKey(ACTION_PARAM_KEY),
                    value = converter.convert(actionName),
                )
            )
            addAll(getInvokeParams(method.parameters, args))
        }
        return invokeParams
    }

    private fun getInvokeParams(parameters: Array<Parameter>, args: Array<out Any>?) =
        parameters.indices.map { i ->
            requireNotNull(args)
            DataEntry(
                key = DataKey(parameters[i].takeName()),
                value = converter.convert(args[i])
            )
        }

    private fun Parameter.takeName() = getAnnotation(InvokeParam::class.java)?.name ?: name

    companion object {
        const val ACTION_PARAM_KEY = "action"
    }
}
