package com.wavesenterprise.sdk.contract.client.invocation.factory

import com.wavesenterprise.sdk.contract.client.invocation.ContractHandlerInvocationHandler
import com.wavesenterprise.sdk.contract.client.invocation.ParamsBuilder
import com.wavesenterprise.sdk.contract.client.invocation.TxTypeResolver
import com.wavesenterprise.sdk.node.domain.DataEntry
import com.wavesenterprise.sdk.node.domain.TxType

class ContractHandlerInvocationHandlerFactory(
    val paramsBuilder: ParamsBuilder,
    val txTypeResolver: TxTypeResolver,
) {
    fun handleContractInvocation(receiver: (List<DataEntry>, TxType) -> (Unit)) =
        ContractHandlerInvocationHandler(paramsBuilder, txTypeResolver, receiver)
}
