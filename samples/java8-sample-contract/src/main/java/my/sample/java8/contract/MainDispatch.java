package my.sample.java8.contract;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wavesenterprise.sdk.contract.core.dispatch.ContractDispatcher;
import com.wavesenterprise.sdk.contract.grpc.GrpcJacksonContractDispatcherBuilder;

public class MainDispatch {
    public static void main(String[] args) {
        ContractDispatcher contractDispatcher = GrpcJacksonContractDispatcherBuilder.builder()
                .contractHandlerType(SampleContractHandler.class)
                .objectMapper(getObjectMapper())
                .build();

        contractDispatcher.dispatch();
    }

    private static ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }
}
