package com.wavesenterprise.sdk.contract.api.state

import com.wavesenterprise.sdk.node.domain.DataValue

interface ContractToDataValueConverter {
    fun <T> convert(value: T): DataValue
}
