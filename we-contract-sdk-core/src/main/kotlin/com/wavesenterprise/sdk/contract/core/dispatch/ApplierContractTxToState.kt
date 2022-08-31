package com.wavesenterprise.sdk.contract.core.dispatch

import com.wavesenterprise.sdk.contract.api.state.ContractState
import com.wavesenterprise.sdk.contract.core.process.ContractHandlerFactoryImpl
import com.wavesenterprise.sdk.contract.core.process.ContractInvocationArgumentsExtractor
import com.wavesenterprise.sdk.contract.core.process.ContractInvocationMethodExtractor
import com.wavesenterprise.sdk.contract.core.process.ContractTransactionProcessor
import com.wavesenterprise.sdk.contract.core.state.factory.ContractStateFactory
import com.wavesenterprise.sdk.node.domain.contract.ContractTransaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ApplierContractTxToState(
    private val contractHandlerType: Class<*>,
    private val contractInvocationMethodExtractor: ContractInvocationMethodExtractor,
    private val contractInvocationArgumentsExtractor: ContractInvocationArgumentsExtractor,
    private val contractStateFactory: ContractStateFactory,
) {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(ApplierContractTxToState::class.java)
    }

    fun contractTxToState(contractTransaction: ContractTransaction): ContractState {
        logger.debug("Applying transaction with ID = ${contractTransaction.id.asBase58String()}")
        val contractState = contractStateFactory.buildContractState(
            contractTransaction.contractId
        )
        val contractHandlerFactory = ContractHandlerFactoryImpl(
            contractState = contractState,
            contractHandlerType = contractHandlerType,
        )
        ContractTransactionProcessor(
            contractInvocationMethodExtractor = contractInvocationMethodExtractor,
            contractInvocationArgumentsExtractor = contractInvocationArgumentsExtractor,
            contractHandlerFactory = contractHandlerFactory,
        ).process(contractTransaction)
        logger.debug(
            "Successfully applied transaction with ID = ${contractTransaction.id.asBase58String()} " +
                "to virtual state"
        )
        return contractState
    }
}
