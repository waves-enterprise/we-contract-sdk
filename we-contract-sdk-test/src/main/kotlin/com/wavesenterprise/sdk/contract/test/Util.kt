package com.wavesenterprise.sdk.contract.test

import java.util.UUID

object Util {
    fun randomBytesFromUUID() = UUID.randomUUID().toString().toByteArray()
}
