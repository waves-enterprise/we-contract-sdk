package my.sample.kotlin.contract.rockps.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.wavesenterprise.sdk.contract.client.invocation.factory.ContractBlockingClientFactory
import com.wavesenterprise.sdk.contract.client.invocation.factory.ContractClientParams
import com.wavesenterprise.sdk.contract.jackson.JacksonConverterFactory
import com.wavesenterprise.sdk.node.client.feign.FeignNodeClientParams
import com.wavesenterprise.sdk.node.client.feign.FeignProperties
import com.wavesenterprise.sdk.node.client.feign.FeignWeApiFactory
import com.wavesenterprise.sdk.node.client.feign.factory.FeignNodeServiceFactory
import com.wavesenterprise.sdk.node.client.feign.tx.FeignTxService
import com.wavesenterprise.sdk.node.client.feign.tx.WeTxApiFeign
import com.wavesenterprise.sdk.node.domain.Address
import com.wavesenterprise.sdk.node.domain.Fee
import com.wavesenterprise.sdk.node.domain.Hash
import com.wavesenterprise.sdk.node.domain.TxVersion
import com.wavesenterprise.sdk.node.domain.blocking.node.NodeBlockingServiceFactory
import com.wavesenterprise.sdk.node.domain.blocking.tx.TxService
import com.wavesenterprise.sdk.node.domain.contract.ContractId
import com.wavesenterprise.sdk.node.domain.contract.ContractImage
import com.wavesenterprise.sdk.node.domain.contract.ContractName
import com.wavesenterprise.sdk.node.domain.contract.ContractVersion
import com.wavesenterprise.sdk.node.domain.sign.builder.ContractSignRequestBuilder
import com.wavesenterprise.sdk.tx.signer.node.factory.TxServiceTxSignerFactory
import feign.Logger
import my.sample.kotlin.contract.rockps.RockPaperScissorsContractImpl
import my.sample.kotlin.contract.rockps.api.RockPaperScissorsContract
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(
    ContractProperties::class,
    NodeProperties::class,
)
class ContractConfiguration {

    @Bean
    fun contractBlockingClientFactory(
        converterFactory: JacksonConverterFactory,
        contractClientParams: ContractClientParams,
        contractSignRequestBuilder: ContractSignRequestBuilder,
        nodeBlockingServiceFactory: NodeBlockingServiceFactory,
    ) =
        ContractBlockingClientFactory(
            contractClass = RockPaperScissorsContractImpl::class.java,
            contractInterface = RockPaperScissorsContract::class.java,
            converterFactory = converterFactory,
            contractClientProperties = contractClientParams,
            contractSignRequestBuilder = contractSignRequestBuilder,
            nodeBlockingServiceFactory = nodeBlockingServiceFactory,
        )

    @Bean
    fun converterFactory(
        objectMapper: ObjectMapper,
    ) = JacksonConverterFactory(objectMapper)

    @Bean
    fun contractClientParams() = ContractClientParams(localValidationEnabled = true)

    @Bean
    fun contractSignRequestBuilder(
        contractProperties: ContractProperties,
    ) =
        ContractSignRequestBuilder()
            .senderAddress(Address.fromBase58(contractProperties.senderAddress))
            .fee(Fee(0L))
            .contractId(ContractId.fromBase58(contractProperties.contractId))
            .contractVersion(ContractVersion(contractProperties.contractVersion))
            .version(TxVersion(contractProperties.version))
            .image(ContractImage(contractProperties.image))
            .imageHash(Hash.fromHexString(contractProperties.imageHash))
            .contractName(ContractName(contractProperties.contractName))

    @Bean
    fun nodeBlockingServiceFactory(
        feignProperties: FeignNodeClientParams,
    ) =
        FeignNodeServiceFactory(
            FeignNodeClientParams(
                url = feignProperties.url,
                decode404 = feignProperties.decode404,
                connectTimeout = feignProperties.connectTimeout,
                readTimeout = feignProperties.readTimeout,
            )
        )

    @Bean
    fun txService(
        feignProperties: FeignProperties,
    ) = FeignTxService(
        weTxApiFeign = FeignWeApiFactory.createClient(
            clientClass = WeTxApiFeign::class.java,
            feignProperties = feignProperties,
        )
    )

    @Bean
    fun feignProperties(
        nodeProperties: NodeProperties,
    ) = FeignNodeClientParams(
        url = nodeProperties.feign.url,
        decode404 = nodeProperties.feign.decode404,
        connectTimeout = nodeProperties.feign.connectTimeout,
        readTimeout = nodeProperties.feign.readTimeout,
        loggerLevel = Logger.Level.FULL,
    )

    @Bean
    @Primary
    fun objectMapper(): ObjectMapper = ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .registerModule(JavaTimeModule())
        .registerModule(
            KotlinModule.Builder()
                .configure(KotlinFeature.NullIsSameAsDefault, true)
                .build()
        )

    @Bean
    fun txSignerFactory(
        txService: TxService,
    ) = TxServiceTxSignerFactory(
        txService = txService,
    )
}
