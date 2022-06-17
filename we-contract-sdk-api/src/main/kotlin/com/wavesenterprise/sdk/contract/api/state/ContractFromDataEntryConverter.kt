package com.wavesenterprise.sdk.contract.api.state

import com.wavesenterprise.sdk.node.domain.DataEntry

interface ContractFromDataEntryConverter {
    fun <T> convert(dataEntry: DataEntry, valueType: Class<T>): T
    fun <T> convert(dataEntry: DataEntry, typeReference: TypeReference<T>): T
}
