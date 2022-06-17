package com.wavesenterprise.sdk.contract.grpc.connect

import io.grpc.Channel

interface GrpcChannelProvider {
    fun getChannel(grpcConnectionProperties: GrpcConnectionProperties): Channel
}
