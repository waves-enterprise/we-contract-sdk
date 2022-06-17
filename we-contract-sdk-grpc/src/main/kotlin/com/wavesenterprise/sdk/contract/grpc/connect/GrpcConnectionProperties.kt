package com.wavesenterprise.sdk.contract.grpc.connect

interface GrpcConnectionProperties {
    val nodeHost: String
    val nodePort: Int
    val connectionId: String
    val authToken: String
    val keepAliveSeconds: Long
}
