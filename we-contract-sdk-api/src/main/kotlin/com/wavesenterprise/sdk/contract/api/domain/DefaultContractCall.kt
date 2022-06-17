package com.wavesenterprise.sdk.contract.api.domain

import com.wavesenterprise.sdk.node.domain.Address
import com.wavesenterprise.sdk.node.domain.Timestamp
import com.wavesenterprise.sdk.node.domain.TxId
import com.wavesenterprise.sdk.node.domain.contract.ContractTransaction

/**
 * Represents contract call made by a certain address
 */
data class DefaultContractCall(
    override val id: TxId,
    override val sender: Address,
    override val timestamp: Timestamp
) : ContractCall {

    constructor(tx: ContractTransaction) : this(
        id = tx.id,
        sender = tx.sender,
        tx.timestamp
    )

    override val caller: String
        get() = sender.asBase58String()
}
