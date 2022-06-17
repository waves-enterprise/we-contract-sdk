package com.wavesenterprise.sdk.contract.grpc.connect

import io.grpc.Channel
import io.grpc.ManagedChannelBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

class DefaultGrpcChannelProvider : GrpcChannelProvider {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(GrpcChannelProvider::class.java)
    }

    override fun getChannel(
        grpcConnectionProperties: GrpcConnectionProperties,
    ): Channel = grpcConnectionProperties.run {
        ManagedChannelBuilder.forAddress(nodeHost, nodePort)
            .keepAliveWithoutCalls(true)
            .keepAliveTime(keepAliveSeconds, TimeUnit.SECONDS)
            .usePlaintext().build().also {
                logger.debug("Successfully connected to $nodeHost:$nodePort with keepAlive = $keepAliveSeconds")
            }
    }
}
