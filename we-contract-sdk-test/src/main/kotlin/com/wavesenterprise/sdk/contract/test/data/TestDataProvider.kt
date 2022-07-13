package com.wavesenterprise.sdk.contract.test.data

import com.wavesenterprise.sdk.contract.test.Util.randomBytesFromUUID
import com.wavesenterprise.sdk.node.domain.Address
import com.wavesenterprise.sdk.node.domain.AssetId
import com.wavesenterprise.sdk.node.domain.Fee
import com.wavesenterprise.sdk.node.domain.PublicKey
import com.wavesenterprise.sdk.node.domain.Signature
import com.wavesenterprise.sdk.node.domain.Timestamp
import com.wavesenterprise.sdk.node.domain.TxId
import com.wavesenterprise.sdk.node.domain.TxType
import com.wavesenterprise.sdk.node.domain.TxVersion
import com.wavesenterprise.sdk.node.domain.contract.CallContractTransaction
import com.wavesenterprise.sdk.node.domain.contract.ContractId
import com.wavesenterprise.sdk.node.domain.contract.ContractImage
import com.wavesenterprise.sdk.node.domain.contract.ContractImageHash
import com.wavesenterprise.sdk.node.domain.contract.ContractName
import com.wavesenterprise.sdk.node.domain.contract.ContractTransaction
import com.wavesenterprise.sdk.node.domain.contract.ContractVersion
import com.wavesenterprise.sdk.node.domain.contract.CreateContractTransaction
import java.time.Instant
import java.util.UUID

class TestDataProvider private constructor() {

    companion object {
        @JvmStatic
        fun address() = Address(randomBytesFromUUID())

        @JvmStatic
        fun txId() = TxId(randomBytesFromUUID())

        @JvmStatic
        fun contractTransaction(
            sender: Address,
            type: TxType = TxType.CALL_CONTRACT,
        ): ContractTransaction =
            when (type) {
                TxType.CALL_CONTRACT -> CallContractTransaction(
                    id = txId(),
                    sender = sender,
                    contractId = ContractId(txId()),
                    params = emptyList(),
                    fee = Fee(0),
                    type = type,
                    version = TxVersion.fromInt(0),
                    proof = Signature(randomBytesFromUUID()),
                    timestamp = Timestamp.fromUtcTimestamp(Instant.now().toEpochMilli()),
                    contractVersion = ContractVersion(0),
                    feeAssetId = AssetId(randomBytesFromUUID()),
                    senderPublicKey = PublicKey(randomBytesFromUUID())
                )
                TxType.CREATE_CONTRACT -> CreateContractTransaction(
                    id = txId(),
                    sender = sender,
                    contractId = ContractId(txId()),
                    params = emptyList(),
                    fee = Fee(0),
                    type = type,
                    version = TxVersion.fromInt(0),
                    proof = Signature(randomBytesFromUUID()),
                    timestamp = Timestamp.fromUtcTimestamp(Instant.now().toEpochMilli()),
                    feeAssetId = AssetId(randomBytesFromUUID()),
                    senderPublicKey = PublicKey(randomBytesFromUUID()),
                    contractName = ContractName("test-contract-${UUID.randomUUID()}"),
                    image = ContractImage("image"),
                    imageHash = ContractImageHash("imageHash"),
                )
                else -> throw IllegalArgumentException(
                    "Only CALL_CONTRACT and CREATE_CONTRACT are allowed as transaction type"
                )
            }
    }
}
