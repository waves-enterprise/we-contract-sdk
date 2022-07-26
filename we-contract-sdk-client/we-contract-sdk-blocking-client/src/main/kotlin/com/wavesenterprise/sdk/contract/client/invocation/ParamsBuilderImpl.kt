package com.wavesenterprise.sdk.contract.client.invocation

import com.wavesenterprise.sdk.contract.api.annotation.InvokeParam
import com.wavesenterprise.sdk.contract.api.state.ContractToDataValueConverter
import com.wavesenterprise.sdk.node.domain.DataEntry
import com.wavesenterprise.sdk.node.domain.DataKey
import java.lang.reflect.Method
import java.lang.reflect.Parameter

class ParamsBuilderImpl(
    private val converter: ContractToDataValueConverter,
) : ParamsBuilder {

    override fun build(method: Method, args: Array<out Any>?): List<DataEntry> {
        val actionName = method.name
        val invokeParams: MutableList<DataEntry> = getInvokeParams(method.parameters, args)
        invokeParams.add(
            index = 0,
            element = DataEntry(
                key = DataKey(ACTION_PARAM_KEY),
                value = converter.convert(actionName),
            )
        )
        return invokeParams
    }

    private fun getInvokeParams(parameters: Array<Parameter>, args: Array<out Any>?) =
        buildList {
            parameters.indices.forEach { i ->
                val parameter = parameters[i]
                args?.get(i)?.let {
                    val invokeParam: InvokeParam? = parameter.getAnnotation(InvokeParam::class.java)
                    val dataEntry = DataEntry(
                        key = DataKey(invokeParam?.name ?: parameter.name),
                        value = converter.convert(args[i])
                    )
                    add(dataEntry)
                }
            }
        }.toMutableList()

    companion object {
        const val ACTION_PARAM_KEY = "action"
    }
}
