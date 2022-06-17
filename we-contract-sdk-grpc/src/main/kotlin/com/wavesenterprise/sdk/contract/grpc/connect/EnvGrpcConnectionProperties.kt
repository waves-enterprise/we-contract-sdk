package com.wavesenterprise.sdk.contract.grpc.connect

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class EnvGrpcConnectionProperties : GrpcConnectionProperties {
    override val nodeHost: String
    override val nodePort: Int
    override val connectionId: String
    override val keepAliveSeconds: Long = 10L
    override val authToken: String

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(EnvGrpcConnectionProperties::class.java)
    }

    init {
        val envMap = System.getenv()
        nodeHost = requireNotNull(envMap[NODE_HOST_PROP]) {
            "Node host is not specified (env property '$NODE_HOST_PROP')"
        }
        nodePort = requireNotNull(envMap[NODE_PORT_PROP]) {
            "Node port is not specified (env property '$NODE_PORT_PROP')"
        }.toInt()
        connectionId = requireNotNull(envMap[CONNECTION_ID_PROP]) {
            "Connection ID is not specified (env property '$CONNECTION_ID_PROP')"
        }
        authToken = requireNotNull(envMap[AUTH_TOKEN_PROP]) {
            "Auth token is not specified (env property '$AUTH_TOKEN_PROP')"
        }
        logger.debug(
            "Environment grpc connection properties - " +
                "$nodeHost:$nodePort; connectionId = $connectionId; authToken = $authToken"
        )
    }
}

const val NODE_HOST_PROP = "NODE"
const val NODE_PORT_PROP = "NODE_PORT"
const val CONNECTION_ID_PROP = "CONNECTION_ID"
const val AUTH_TOKEN_PROP = "CONNECTION_TOKEN"
