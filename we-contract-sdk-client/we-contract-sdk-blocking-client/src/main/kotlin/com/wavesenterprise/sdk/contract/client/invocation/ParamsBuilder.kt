package com.wavesenterprise.sdk.contract.client.invocation

import com.wavesenterprise.sdk.node.domain.DataEntry
import java.lang.reflect.Method

interface ParamsBuilder {
    fun build(method: Method, args: Array<out Any>): List<DataEntry>
}
