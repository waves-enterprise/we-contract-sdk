package com.wavesenterprise.sdk.contract.core.dispatch

import com.wavesenterprise.sdk.contract.api.exception.RecoverableException
import com.wavesenterprise.sdk.contract.api.state.ContractFromDataEntryConverter
import com.wavesenterprise.sdk.contract.core.process.ContractHandlerInvocationExtractor
import com.wavesenterprise.sdk.contract.core.process.ContractInvocationArgumentsExtractorImpl
import com.wavesenterprise.sdk.contract.core.state.factory.ContractStateFactory
import com.wavesenterprise.sdk.node.client.blocking.contract.ContractService
import com.wavesenterprise.sdk.node.domain.contract.ConnectionRequest
import com.wavesenterprise.sdk.node.domain.contract.ContractTransactionResponse
import com.wavesenterprise.sdk.node.domain.contract.ExecutionErrorRequest
import com.wavesenterprise.sdk.node.domain.contract.ExecutionSuccessRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.debug
import org.slf4j.error
import org.slf4j.info
import java.lang.reflect.InvocationTargetException
import java.util.concurrent.Executor
import kotlin.system.exitProcess

private const val ERROR_EXIT_CODE = 7

// extract interface - move to api
class ContractDispatcher(
    private val connectContractService: ContractService,
    private val txContractService: ContractService,
    private val contractStateFactory: ContractStateFactory,
    private val contractFromDataEntryConverter: ContractFromDataEntryConverter,
    private val contractHandlerType: Class<*>,
    private val executor: Executor,
    private val connectionId: String,
    private val preExecutionHook: (ContractTransactionResponse) -> (Unit) = {},
    private val postExecutionHook: (ContractTransactionResponse) -> (Unit) = {},
) {

    private val contractTxToStateApplier = ContractTxToStateApplier(
        contractHandlerType = contractHandlerType,
        contractInvocationMethodExtractor = ContractHandlerInvocationExtractor(contractHandlerType),
        contractInvocationArgumentsExtractor = ContractInvocationArgumentsExtractorImpl(contractFromDataEntryConverter),
        contractStateFactory = contractStateFactory,
    )

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(ContractDispatcher::class.java)
    }

    fun dispatch() {
        logger.info("Dispatching contract for handler class ${contractHandlerType.canonicalName}")
        val contractTransactionIterator = connectContractService.connect(
            ConnectionRequest(connectionId)
        ).iterator()

        contractTransactionIterator.forEachRemaining { contractTransactionResponse ->
            executor.execute {
                logger.debug {
                    "Received transaction with ID = ${contractTransactionResponse.txId().asBase58String()}"
                }
                preExecutionHook(contractTransactionResponse)
                try {
                    val contractState =
                        contractTxToStateApplier.contractTxToState(contractTransactionResponse.transaction)
                    txContractService.commitExecutionSuccess(
                        ExecutionSuccessRequest(
                            txId = contractTransactionResponse.txId(),
                            results = contractState.results()
                        )
                    )
                    logger.info {
                        "Successfully applied transaction with ID = ${
                        contractTransactionResponse.txId().asBase58String()
                        } to state"
                    }
                } catch (invocationTargetException: InvocationTargetException) {
                    logger.error(invocationTargetException) {
                        "Exception in contract method while processing transaction with ID = " +
                            contractTransactionResponse.txId().asBase58String()
                    }
                    tryCommitError(
                        contractTransactionResponse = contractTransactionResponse,
                        cause = invocationTargetException.cause ?: invocationTargetException
                    )
                } catch (ex: Exception) {
                    logger.error(ex) {
                        "Exception while processing transaction with ID = " +
                            contractTransactionResponse.txId().asBase58String()
                    }
                    tryCommitError(contractTransactionResponse, ex)
                } catch (error: Error) {
                    logger.error(error) {
                        "Error while processing transaction with ID = " +
                            contractTransactionResponse.txId().asBase58String()
                    }
                    exitProcess(ERROR_EXIT_CODE)
                } finally {
                    postExecutionHook(contractTransactionResponse)
                }
            }
        }
    }

    private fun tryCommitError(
        contractTransactionResponse: ContractTransactionResponse,
        cause: Throwable,
    ) {
        try {
            txContractService.commitExecutionError(
                ExecutionErrorRequest(
                    txId = contractTransactionResponse.txId(),
                    message = cause.message ?: "<empty message>",
                    code = when (cause) {
                        is RecoverableException -> ExecutionErrorRequest.ErrorCode.RECOVERABLE_ERROR
                        else -> ExecutionErrorRequest.ErrorCode.FATAL_ERROR
                    },
                )
            )
        } catch (commitException: Exception) {
            logger.error(
                "Error while committing execution error for exception ${cause.javaClass.canonicalName}",
                commitException
            )
            throw commitException
        }
    }

    private fun ContractTransactionResponse.txId() = transaction.id
}
