package com.wavesenterprise.sdk.contract.api.domain

import com.wavesenterprise.sdk.node.domain.Address
import com.wavesenterprise.sdk.node.domain.Timestamp
import com.wavesenterprise.sdk.node.domain.TxId

sealed interface ContractCall {
    val id: TxId
    val sender: Address
    val caller: String // can be Address or ContractId
    val timestamp: Timestamp
}
