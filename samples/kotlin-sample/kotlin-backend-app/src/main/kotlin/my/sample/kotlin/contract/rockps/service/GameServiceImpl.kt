package my.sample.kotlin.contract.rockps.service

import com.wavesenterprise.sdk.contract.client.invocation.factory.ContractBlockingClientFactory
import com.wavesenterprise.sdk.contract.client.invocation.factory.ExecutionContext
import com.wavesenterprise.sdk.node.domain.Address
import com.wavesenterprise.sdk.node.domain.Password
import com.wavesenterprise.sdk.node.domain.contract.ContractId
import com.wavesenterprise.sdk.node.domain.tx.ContractTx
import com.wavesenterprise.sdk.tx.signer.node.credentials.Credentials
import com.wavesenterprise.sdk.tx.signer.node.factory.TxServiceTxSignerFactory
import my.sample.kotlin.contract.rockps.api.RockPaperScissorsContract
import my.sample.kotlin.contract.rockps.game.request.CreateGameRequest
import my.sample.kotlin.contract.rockps.game.request.PlayRequest
import my.sample.kotlin.contract.rockps.game.request.RevealRequest
import org.springframework.stereotype.Service

@Service
class GameServiceImpl(
    private val contractBlockingClientFactory: ContractBlockingClientFactory<RockPaperScissorsContract>,
    private val txSignerFactory: TxServiceTxSignerFactory,
) : GameService {

    override fun createGame(
        createGameRequest: CreateGameRequest,
        address: String,
        password: String,
    ): ContractTx {
        val txSigner = txSignerFactory.withCredentials(
            Credentials(
                senderAddress = Address.fromBase58(address),
                password = Password(password),
            )
        )
        val executionContext: ExecutionContext = contractBlockingClientFactory.executeContract(
            txSigner = txSigner
        ) { contract ->
            contract.createGame(createGameRequest)
        }
        return executionContext.tx
    }

    override fun play(
        playRequest: PlayRequest,
        address: String,
        password: String,
        contractId: String,
    ): ContractTx {
        val txSigner = txSignerFactory.withCredentials(
            Credentials(
                senderAddress = Address.fromBase58(address),
                password = Password(password),
            )
        )
        val executionContext: ExecutionContext = contractBlockingClientFactory.executeContract(
            contractId = ContractId.fromBase58(contractId),
            txSigner = txSigner
        ) { contract ->
            contract.play(playRequest)
        }
        return executionContext.tx
    }

    override fun reveal(
        revealRequest: RevealRequest,
        address: String,
        password: String,
        contractId: String,
    ): ContractTx {
        val txSigner = txSignerFactory.withCredentials(
            Credentials(
                senderAddress = Address.fromBase58(address),
                password = Password(password),
            )
        )
        val executionContext: ExecutionContext = contractBlockingClientFactory.executeContract(
            contractId = ContractId.fromBase58(contractId),
            txSigner = txSigner
        ) { contract ->
            contract.reveal(revealRequest)
        }
        return executionContext.tx
    }
}
