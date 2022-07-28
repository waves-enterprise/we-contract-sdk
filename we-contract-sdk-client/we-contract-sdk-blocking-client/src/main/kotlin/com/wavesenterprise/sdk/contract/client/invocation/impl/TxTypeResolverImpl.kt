package com.wavesenterprise.sdk.contract.client.invocation.impl

import com.wavesenterprise.sdk.contract.api.annotation.ContractInit
import com.wavesenterprise.sdk.contract.client.invocation.TxTypeResolver
import com.wavesenterprise.sdk.node.domain.TxType
import java.lang.reflect.Method

class TxTypeResolverImpl : TxTypeResolver {
    override fun resolve(method: Method) =
        if (method.getAnnotation(ContractInit::class.java) != null) {
            TxType.CREATE_CONTRACT
        } else {
            TxType.CALL_CONTRACT
        }
}
