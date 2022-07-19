package com.wavesenterprise.sdk.contract.grpc

import com.fasterxml.jackson.databind.ObjectMapper
import com.wavesenterprise.sdk.contract.api.domain.ContractConcurrency
import com.wavesenterprise.sdk.contract.api.domain.toThreadCount
import com.wavesenterprise.sdk.contract.api.state.NodeContractStateValuesProvider
import com.wavesenterprise.sdk.contract.core.dispatch.ContractDispatcher
import com.wavesenterprise.sdk.contract.core.state.factory.ContractStateFactory
import com.wavesenterprise.sdk.contract.core.state.factory.DefaultBackingMapContractStateFactory
import com.wavesenterprise.sdk.contract.grpc.connect.DefaultGrpcChannelProvider
import com.wavesenterprise.sdk.contract.grpc.connect.EnvGrpcConnectionProperties
import com.wavesenterprise.sdk.contract.grpc.connect.GrpcChannelProvider
import com.wavesenterprise.sdk.contract.grpc.connect.GrpcConnectionProperties
import com.wavesenterprise.sdk.contract.grpc.connect.HeaderClientInterceptor
import com.wavesenterprise.sdk.contract.grpc.connect.auth.AuthTokenSupplier
import com.wavesenterprise.sdk.contract.grpc.connect.auth.ThreadLocalAuthTokenSupplier
import com.wavesenterprise.sdk.contract.grpc.node.GrpcBlockingClientNodeContractStateValuesProvider
import com.wavesenterprise.sdk.contract.jackson.JacksonContractToDataValueConverter
import com.wavesenterprise.sdk.contract.jackson.JacksonFromDataEntryConverter
import com.wavesenterprise.sdk.node.domain.blocking.contract.ContractService
import com.wavesenterprise.sdk.node.domain.contract.AuthToken
import com.wavesenterprise.sdk.node.domain.grpc.blocking.contract.ContractGrpcBlockingService
import io.grpc.Channel
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class GrpcJacksonContractDispatcherBuilder {

    private var contractHandlerType: Class<*>? = null
    private var objectMapper: ObjectMapper? = null
    private var executor: Executor? = null
    private var contractConcurrency: ContractConcurrency? = null
    private var txContractService: ContractService? = null
    private var connectContractService: ContractService? = null
    private var grpcConnectionProperties: GrpcConnectionProperties? = null
    private var channelProvider: GrpcChannelProvider? = null

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(GrpcJacksonContractDispatcherBuilder::class.java)

        @JvmStatic
        fun builder(): GrpcJacksonContractDispatcherBuilder = GrpcJacksonContractDispatcherBuilder()
    }

    fun contractHandlerType(contractHandlerType: Class<*>): GrpcJacksonContractDispatcherBuilder = this.apply {
        this.contractHandlerType = contractHandlerType
    }

    fun objectMapper(objectMapper: ObjectMapper): GrpcJacksonContractDispatcherBuilder = this.apply {
        this.objectMapper = objectMapper
    }

    fun executor(executor: Executor): GrpcJacksonContractDispatcherBuilder = this.apply {
        this.executor = executor
    }

    fun contractConcurrency(contractConcurrency: ContractConcurrency): GrpcJacksonContractDispatcherBuilder =
        this.apply {
            this.contractConcurrency = contractConcurrency
        }

    fun txContractService(txContractService: ContractService): GrpcJacksonContractDispatcherBuilder = this.apply {
        this.txContractService = txContractService
    }

    fun connectContractService(connectContractService: ContractService): GrpcJacksonContractDispatcherBuilder =
        this.apply {
            this.connectContractService = connectContractService
        }

    fun channelProvider(grpcChannelProvider: GrpcChannelProvider): GrpcJacksonContractDispatcherBuilder =
        this.apply {
            this.channelProvider = grpcChannelProvider
        }

    fun grpcConnectionProperties(
        grpcConnectionProperties: GrpcConnectionProperties,
    ): GrpcJacksonContractDispatcherBuilder = this.apply {
        this.grpcConnectionProperties = grpcConnectionProperties
    }

    fun build(): ContractDispatcher {
        val actualContractHandlerType = requireNotNull(contractHandlerType) {
            "Contract handler class must be specified"
        }
        logger.info("Building Jackson GRPC contract handler dispatcher ${actualContractHandlerType.canonicalName}")

        val actualObjectMapper = objectMapper ?: defaultObjectMapper()
        val actualContractConcurrency = contractConcurrency ?: ContractConcurrency.IO
        val actualExecutor = executor ?: defaultExecutor(actualContractConcurrency)

        val toDataValueConverter = JacksonContractToDataValueConverter(actualObjectMapper)
        val fromDataEntryConverter = JacksonFromDataEntryConverter(actualObjectMapper)

        val actualConnectionProperties = grpcConnectionProperties ?: EnvGrpcConnectionProperties()
        val actualChannelProvider = channelProvider ?: DefaultGrpcChannelProvider()
        val channel = actualChannelProvider.getChannel(
            grpcConnectionProperties = actualConnectionProperties
        )
        val defaultTxContractService = getTxContractService(channel)
        val defaultConnectContractService = getConnectContractService(channel, actualConnectionProperties)

        val nodeContractStateProvider: NodeContractStateValuesProvider =
            GrpcBlockingClientNodeContractStateValuesProvider(
                contractService = txContractService ?: defaultTxContractService
            )

        val contractStateFactory: ContractStateFactory = DefaultBackingMapContractStateFactory(
            nodeContractStateValuesProvider = nodeContractStateProvider,
            contractFromDataEntryConverter = fromDataEntryConverter,
            contractToDataValueConverter = toDataValueConverter,
        )

        return ContractDispatcher(
            connectContractService = connectContractService ?: defaultConnectContractService,
            txContractService = txContractService ?: defaultTxContractService,
            contractStateFactory = contractStateFactory,
            contractFromDataEntryConverter = fromDataEntryConverter,
            contractHandlerType = actualContractHandlerType,
            executor = actualExecutor,
            connectionId = actualConnectionProperties.connectionId,
            preExecutionHook = { ThreadLocalAuthTokenSupplier.setToken(it.authToken) },
            postExecutionHook = { ThreadLocalAuthTokenSupplier.clearToken() }
        )
    }

    private fun defaultExecutor(actualContractConcurrency: ContractConcurrency): Executor =
        Executors.newFixedThreadPool(
            actualContractConcurrency.toThreadCount(Runtime.getRuntime().availableProcessors())
        )

    private fun defaultObjectMapper(): ObjectMapper = ObjectMapper()

    private fun getConnectContractService(
        channel: Channel,
        connectionProperties: GrpcConnectionProperties,
    ): ContractService = ContractGrpcBlockingService(
        channel = channel,
        clientInterceptors = listOf(
            HeaderClientInterceptor(connectionProperties.authTokenSupplier())
        )
    )

    private fun getTxContractService(channel: Channel): ContractService = ContractGrpcBlockingService(
        channel = channel,
        clientInterceptors = listOf(
            HeaderClientInterceptor(
                ThreadLocalAuthTokenSupplier()
            )
        )
    )

    private fun GrpcConnectionProperties.authTokenSupplier() = object : AuthTokenSupplier {
        override fun get(): AuthToken {
            return AuthToken(authToken)
        }
    }
}
