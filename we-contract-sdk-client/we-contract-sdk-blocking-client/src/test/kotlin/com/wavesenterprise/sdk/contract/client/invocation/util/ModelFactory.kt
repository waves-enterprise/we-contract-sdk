package com.wavesenterprise.sdk.contract.client.invocation.util

import com.wavesenterprise.sdk.contract.client.invocation.User
import com.wavesenterprise.sdk.contract.test.Util.randomBytesFromUUID
import com.wavesenterprise.sdk.node.domain.Address
import com.wavesenterprise.sdk.node.domain.AssetId
import com.wavesenterprise.sdk.node.domain.DataEntry
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
import com.wavesenterprise.sdk.node.domain.contract.ContractVersion
import com.wavesenterprise.sdk.node.domain.tx.CallContractTx
import com.wavesenterprise.sdk.node.domain.tx.CreateContractTx
import java.util.UUID

fun callContractTransaction(
    params: List<DataEntry>,
) = CallContractTransaction(
    id = TxId(ByteArray(1)),
    senderPublicKey = PublicKey(ByteArray(1)),
    sender = Address(ByteArray(1)),
    proof = Signature(ByteArray(1)),
    fee = Fee(1L),
    type = TxType.CALL_CONTRACT,
    params = params,
    version = TxVersion(1),
    timestamp = Timestamp(1L),
    contractId = ContractId(TxId(ByteArray(1))),
    feeAssetId = AssetId(ByteArray(1)),
    contractVersion = ContractVersion(1),
)

fun user(
    id: UUID = UUID.randomUUID(),
    name: String = "test",
) = User(
    id = id,
    name = name,
)

fun callContractTx(
    params: List<DataEntry> = emptyList(),
) =
    CallContractTx(
        id = TxId(randomBytesFromUUID()),
        senderPublicKey = PublicKey(randomBytesFromUUID()),
        contractId = ContractId(TxId(randomBytesFromUUID())),
        params = params,
        fee = Fee(0L),
        timestamp = Timestamp(0L),
        contractVersion = ContractVersion(1),
        senderAddress = Address(randomBytesFromUUID()),
        version = TxVersion(1),
    )

fun createContractTx(
    params: List<DataEntry> = emptyList(),
    txId: TxId = TxId(randomBytesFromUUID()),
) =
    CreateContractTx(
        id = txId,
        senderPublicKey = PublicKey(randomBytesFromUUID()),
        params = params,
        fee = Fee(0L),
        timestamp = Timestamp(0L),
        senderAddress = Address(randomBytesFromUUID()),
        version = TxVersion(1),
        image = ContractImage("image"),
        imageHash = ContractImageHash("imageHash"),
        contractName = ContractName("contractName")
    )
