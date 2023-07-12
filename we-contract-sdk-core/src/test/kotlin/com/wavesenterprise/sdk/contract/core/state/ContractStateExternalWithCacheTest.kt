package com.wavesenterprise.sdk.contract.core.state

import com.wavesenterprise.sdk.contract.api.state.ContractState
import com.wavesenterprise.sdk.contract.core.utils.contractId
import com.wavesenterprise.sdk.node.domain.contract.ContractId
import io.mockk.Called
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class ContractStateExternalWithCacheTest {

    private var delegate: ContractState = mockk<ContractState>(relaxed = true)
    private var externalStates: MutableMap<ContractId, ContractState> = HashMap()
    private var cache = ContractStateWithCachedExternalContracts(delegate, externalStates)

    @Test
    fun `test`() {
        val contractState = mockk<ContractState>()
        val contractIdInCache = contractId()
        val contractIdNotInCache = contractId()
        externalStates[contractIdInCache] = contractState
        cache.external(contractIdInCache)
        cache.external(contractIdNotInCache)

        assertSame(cache.external(contractIdInCache), contractState)
        verify { delegate.external(contractIdNotInCache) }
        verify { delegate.external(contractIdInCache) wasNot Called }
    }
}
