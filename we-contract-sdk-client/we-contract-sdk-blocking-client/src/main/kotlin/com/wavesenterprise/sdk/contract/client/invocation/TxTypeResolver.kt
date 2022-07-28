package com.wavesenterprise.sdk.contract.client.invocation

import com.wavesenterprise.sdk.node.domain.TxType
import java.lang.reflect.Method

interface TxTypeResolver {
    fun resolve(method: Method): TxType
}
