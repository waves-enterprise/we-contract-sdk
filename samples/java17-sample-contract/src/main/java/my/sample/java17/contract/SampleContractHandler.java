package my.sample.java17.contract;

import com.wavesenterprise.sdk.contract.api.annotation.ContractAction;
import com.wavesenterprise.sdk.contract.api.annotation.ContractHandler;
import com.wavesenterprise.sdk.contract.api.annotation.ContractInit;
import com.wavesenterprise.sdk.contract.api.exception.RecoverableException;
import com.wavesenterprise.sdk.contract.api.state.ContractState;
import com.wavesenterprise.sdk.contract.api.state.TypeReference;
import com.wavesenterprise.sdk.contract.api.state.mapping.Mapping;
import com.wavesenterprise.sdk.node.domain.contract.ContractTransaction;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@ContractHandler
public class SampleContractHandler {

    private final ContractState contractState;
    private final ContractTransaction tx;

    private final Mapping<List<MySampleContractDto>> mapping;

    public SampleContractHandler(ContractState contractState, ContractTransaction tx) {
        this.contractState = contractState;
        mapping = contractState.getMapping(
                new TypeReference<>() {
                }, "SOME_PREFIX");
        this.tx = tx;
    }

    @ContractInit
    public void createContract(String initialParam) {
        contractState.put("INITIAL_PARAM", initialParam);
    }

    @ContractAction
    public void doSomeAction(String dtoId) {
        contractState.put("INITIAL_PARAM", Instant.ofEpochMilli(tx.getTimestamp().getUtcTimestampMillis()));

        if (mapping.has(dtoId)) {
            throw new IllegalArgumentException("Already has " + dtoId + " on state");
        }
        mapping.put(dtoId,
                Arrays.asList(
                        new MySampleContractDto("john", 18),
                        new MySampleContractDto("harry", 54)
                ));
    }

    @ContractAction
    public void doActionWithErrorLogic(String initialParam) throws Exception {
        long utcTimestampMillis = tx.getTimestamp().getUtcTimestampMillis();
        if (utcTimestampMillis % 2 == 0) {
            throw new MyException("Tx timestamp is " + utcTimestampMillis + " which is odd. Throwing Error");
            // throwing checked exeception for retry
        }
        contractState.put("success-" + utcTimestampMillis, "hooray");
    }

    static class MyException extends Exception implements RecoverableException {
        public MyException(String message) {
            super(message);
        }
    }
}
