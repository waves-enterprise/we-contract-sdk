package com.wavesenterprise.sdk.contract.core.utils

import com.wavesenterprise.sdk.node.domain.DataEntry
import com.wavesenterprise.sdk.node.domain.DataKey
import com.wavesenterprise.sdk.node.domain.DataValue
import com.wavesenterprise.sdk.node.domain.TxId
import com.wavesenterprise.sdk.node.domain.contract.ContractId
import java.util.UUID

fun contractId(
    txId: TxId = txId()
) = ContractId(
    txId = txId
)

fun txId(
    bytes: ByteArray = UUID.randomUUID().toString().toByteArray(),
) = TxId(
    bytes = bytes,
)

fun dataEntry(
    key: DataKey = dataKey(),
    value: DataValue = stringDataValue(),
) = DataEntry(
    key = key,
    value = value,
)

fun dataKey(
    key: String = "dataKey",
) = DataKey(
    value = key,
)

fun stringDataValue(
    value: String = "stringDataValue",
) = DataValue.StringDataValue(
    value = value,
)
