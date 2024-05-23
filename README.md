# we-contract-sdk

Java/Kotlin contract SDK used for building Docker smart contracts.

All transaction handling is done via methods of a single class marked with `@ContractHandler` annotation. 
The methods which implement handling logic are marked with `@ContractInit` (for CreateContractTx) and `@ContractAction` (for CallContractTx).      

## Module structure.

- **we-contract-sdk-api.**
Contains mainly interfaces and annotations for marking contract handlers and their methods. 
Should be used directly by *-contract-api modules.
- **we-contract-sdk-client.**
The client contract is used to interact with the contract from the backend code of Java/Kotlin applications. [README](we-contract-sdk-client/README.md)
- **we-contract-sdk-core.**
Core processing contract transactions logic. 
Transport and mapping is not implemented in core module and should be provided externally. 
- **we-contract-sdk-jackson.**
Jackson JSON mapping for values stored on state.  
- **we-contract-sdk-grpc.**
Implementation with GRPC transport and Jackson object mapping. 
Should be used directly by the module which starts and runs the contract.
- **we-contract-sdk-test.**
Utils for contract unit testing. 

## Getting started

### Dependencies
#### Maven
```xml
<dependency>
  <groupId>com.wavesenterprise</groupId>
  <artifactId>we-contract-sdk-grpc</artifactId>
  <version>1.1.0</version>
</dependency>
```
#### Gradle
```kotlin
dependencies {
    implementation("com.wavesenterprise:we-contract-sdk-grpc:1.1.0")
} 
```

### Quick start
Other examples can be found in [Samples](samples).
#### 1. Create contract handler
```java
@ContractHandler
public class SampleContractHandler {

    private final ContractState contractState;
    private final ContractTransaction tx;

    private final Mapping<List<MySampleContractDto>> mapping;

    public SampleContractHandler(ContractState contractState, ContractTransaction tx) {
        this.contractState = contractState;
        mapping = contractState.getMapping(
                new TypeReference<List<MySampleContractDto>>() {
                }, "SOME_PREFIX");
        this.tx = tx;
    }
}

```

#### 2. Add `@ContractInit` and `@ContractAction` method to handle contract transactions
```java
public class SampleContractHandler {

    // ... 

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
}
```

#### 3. Dispatch contract with specified contract handler and settings
```java
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
```

#### 4. Create Docker image 

```dockerfile
FROM openjdk:8-alpine
MAINTAINER Waves Enterprise <>

ENV JAVA_MEM="-Xmx256M"
ENV JAVA_OPTS=""

ADD build/libs/*-all.jar app.jar

RUN chmod +x app.jar
RUN eval $SET_ENV_CMD
CMD ["/bin/sh", "-c", "eval ${SET_ENV_CMD} ; java $JAVA_MEM $JAVA_OPTS -jar app.jar"]
```
#### 5. Push to docker repository used by WE Node mining contract transactions 
Publish to registry used by WE Blockchain Node. For convenience there is a bash script [build_and_push_to_docker.sh](samples/java8-sample-contract/build_and_push_to_docker.sh) which builds and pushes to specified registry.
 
```shell
 ./build_and_push_to_docker.sh my.registry.com/contracts/my-awesome-docker-contract:1.0.0
```

#### 6. Sign and broadcast transactions for creating and invoking published contract
You will need `image` and `imageHash` of the published contract to create it.  

CreateContractTx example
```json
{
    "image": "my.registry.com/contracts/my-awesome-docker-contract:1.0.0",
    "fee": 0,
    "imageHash": "d17f6c1823176aa56e0e8184f9c45bc852ee9b076b06a586e40c23abde4d7dfa",
    "type": 103,
    "params": [
        {
            "type": "string",
            "value": "createContract",
            "key": "action"
        },
        {
            "type": "string",
            "value": "initialValue",
            "key": "createContract"
        }
    ],
    "version": 2,
    "sender": "3M3ybNZvLG7o7rnM4F7ViRPnDTfVggdfmRX",
    "feeAssetId": null,
    "contractName": "myAwesomeContract"
}
```
To call contract you will need `contractId = CreateContractTx.id`.

CallContractTx example
```json
{
    "contractId": "7sVc6ybnqZr523xWK5Sg7xADsX597qga8iQNAS9f1D3c",
    "fee": 0,
    "type": 104,
    "params": [
      {
        "type": "string",
        "value": "doSomeAction",
        "key": "action"
      },
      {
        "type": "string",
        "value": "someValue",
        "key": "createContract"
      }
    ],
    "version": 2,
    "sender": "3M3ybNZvLG7o7rnM4F7ViRPnDTfVggdfmRX",
    "feeAssetId": null,
    "contractVersion": 1
}
```
### Notes on usage
#### Usage with Java 11+
Library has been tested against java8 and java17. 
When using with java17 additional java options should be specified for the io.grpc to enable optimizations.

```dockerfile
--add-opens java.base/jdk.internal.misc=ALL-UNNAMED --add-opens=java.base/java.nio=ALL-UNNAMED -Dio.netty.tryReflectionSetAccessible=true
```
Full example can be found in [Dockerfile](samples/java17-sample-contract/Dockerfile) for java17.
