package my.sample.java17.contract.rockps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wavesenterprise.sdk.contract.core.dispatch.ContractDispatcher;
import com.wavesenterprise.sdk.contract.grpc.GrpcJacksonContractDispatcherBuilder;

public class MainDispatch {
    public static void main(String[] args) {
        ContractDispatcher contractDispatcher = GrpcJacksonContractDispatcherBuilder.builder()
                .contractHandlerType(RockPaperScissorsContractImpl.class)
                .objectMapper(getObjectMapper())
                .build();

        contractDispatcher.dispatch();
    }

    private static ObjectMapper getObjectMapper() {
        return JsonMapper.builder().addModule(new JavaTimeModule()).build();
    }
}
