package my.sample.java17.contract.rockps;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wavesenterprise.sdk.contract.api.state.ContractState;
import com.wavesenterprise.sdk.contract.api.state.TypeReference;
import com.wavesenterprise.sdk.contract.api.state.mapping.Mapping;
import com.wavesenterprise.sdk.contract.test.state.ContractTestStateFactory;
import com.wavesenterprise.sdk.node.domain.Address;
import com.wavesenterprise.sdk.node.domain.TxType;
import my.sample.java17.contract.rockps.api.RockPaperScissorsContract;
import my.sample.java17.contract.rockps.game.AnswerType;
import my.sample.java17.contract.rockps.game.Game;
import my.sample.java17.contract.rockps.game.GameStatus;
import my.sample.java17.contract.rockps.game.Player;
import my.sample.java17.contract.rockps.game.request.CreateGameRequest;
import my.sample.java17.contract.rockps.game.request.PlayRequest;
import my.sample.java17.contract.rockps.game.request.RevealRequest;
import my.sample.java17.contract.rockps.util.Util;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.wavesenterprise.sdk.contract.test.data.TestDataProvider.address;
import static com.wavesenterprise.sdk.contract.test.data.TestDataProvider.contractTransaction;
import static my.sample.java17.contract.rockps.api.RockPaperScissorsContract.Keys.*;
import static org.junit.jupiter.api.Assertions.*;

class RockPaperScissorsContractTest {

    JsonMapper objectMapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();

    @Test
    public void shouldThrowErrorWhenMoreThanTwoPlayers() {
        Address player1 = address();
        Address player2 = address();
        Address player3 = address();
        List<Address> players = Arrays.asList(player1, player2, player3);
        ContractState testState = ContractTestStateFactory.state(objectMapper);
        RockPaperScissorsContractImpl rockPaperScissorsContract = new RockPaperScissorsContractImpl(
                testState,
                contractTransaction(address(), TxType.CREATE_CONTRACT)
        );

        assertThrows(IllegalArgumentException.class, () -> rockPaperScissorsContract.createGame(
                gameRequest(players)
        ));
    }

    @Test
    public void shouldCreateGame() {
        Address player1 = address();
        Address player2 = address();
        List<Address> players = Arrays.asList(player1, player2);
        ContractState testState = ContractTestStateFactory.state(objectMapper);
        RockPaperScissorsContractImpl rockPaperScissorsContract = new RockPaperScissorsContractImpl(
                testState,
                contractTransaction(address(), TxType.CREATE_CONTRACT)
        );

        rockPaperScissorsContract.createGame(
                gameRequest(players)
        );

        Set<String> playerAddresses = testState.get(PLAYER_ADDRESSES_KEY, new TypeReference<>() {
        });
        assertEquals(players.size(), playerAddresses.size());
        assertFalse(testState.tryGet(GAME_KEY, Game.class).isPresent());
    }

    @Test
    public void shouldThrowErrorWhenPlayingByNotKnownAddress() {
        Address player1 = address();
        Address player2 = address();
        Address sender = address();
        List<Address> players = Arrays.asList(player1, player2);
        ContractState testState = ContractTestStateFactory.state(objectMapper);
        RockPaperScissorsContractImpl rockPaperScissorsContract = new RockPaperScissorsContractImpl(
                testState,
                contractTransaction(sender, TxType.CALL_CONTRACT)
        );

        rockPaperScissorsContract.createGame(
                gameRequest(players)
        );
        assertThrows(
                IllegalAccessError.class,
                () -> rockPaperScissorsContract.play(new PlayRequest("someHash"))
        );
    }

    @NotNull
    private CreateGameRequest gameRequest(List<Address> players) {
        return new CreateGameRequest(
                players.stream().map(
                        address -> new CreateGameRequest.Player(address.asBase58String())
                ).collect(Collectors.toList())
        );
    }

    @Test
    public void shouldPlay() {
        Address player1 = address();
        Address player2 = address();
        Address sender = address();
        List<Address> players = Arrays.asList(player1, player2);
        ContractState testState = ContractTestStateFactory.state(objectMapper);
        RockPaperScissorsContractImpl rockPaperScissorsContract = new RockPaperScissorsContractImpl(
                testState,
                contractTransaction(sender, TxType.CALL_CONTRACT)
        );

        rockPaperScissorsContract.createGame(
                gameRequest(players)
        );
        assertThrows(
                IllegalAccessError.class,
                () -> rockPaperScissorsContract.play(new PlayRequest("someHash"))
        );
    }

    @Test
    public void playGame() {
        Address currentAddress = address();
        ContractState testState = ContractTestStateFactory.state(objectMapper);
        Mapping<Player> players = testState.getMapping(Player.class, PLAYERS_MAPPING_PREFIX);
        RockPaperScissorsContractImpl rockPaperScissorsContract = new RockPaperScissorsContractImpl(
                testState,
                contractTransaction(currentAddress, TxType.CREATE_CONTRACT)
        );
        String currentAddressStr = currentAddress.asBase58String();
        testState.put(PLAYER_ADDRESSES_KEY, Collections.singletonList(currentAddressStr));
        String hashedAnswer = "hashed";

        rockPaperScissorsContract.play(new PlayRequest(hashedAnswer));

        Player player = players.get(currentAddressStr);
        assertEquals(hashedAnswer, player.getHashedAnswer());
    }

    @Test
    public void shouldRevealResult() {
        Address currentAddress = address();
        ContractState testState = ContractTestStateFactory.state(objectMapper);
        Mapping<Player> players = testState.getMapping(Player.class, PLAYERS_MAPPING_PREFIX);
        RockPaperScissorsContractImpl rockPaperScissorsContract = new RockPaperScissorsContractImpl(
                testState,
                contractTransaction(currentAddress, TxType.CREATE_CONTRACT)
        );
        String currentAddressStr = currentAddress.asBase58String();
        testState.put(PLAYER_ADDRESSES_KEY, Collections.singletonList(currentAddressStr));
        String salt = "asdfb23";
        AnswerType answerType = AnswerType.PAPER;
        String hashedAnswer = Util.hash(answerType + "_" + salt);
        rockPaperScissorsContract.play(new PlayRequest(hashedAnswer));

        rockPaperScissorsContract.reveal(new RevealRequest(salt));

        Player player = players.get(currentAddressStr);
        assertEquals(player.getAnswer(), AnswerType.PAPER);
    }

    @Test
    public void shouldPassIntegrationTest() {
        Address player1 = address();
        String player1Salt = "salt1234";
        AnswerType player1AnswerType = AnswerType.PAPER;
        Address player2 = address();
        String player2Salt = "salt567";
        AnswerType player2AnswerType = AnswerType.SCISSORS;
        Address contractCreator = address();
        ContractState testState = ContractTestStateFactory.state(objectMapper);

        RockPaperScissorsContract rockPaperScissorsContract = new RockPaperScissorsContractImpl(
                testState,
                contractTransaction(contractCreator, TxType.CREATE_CONTRACT)
        );
        rockPaperScissorsContract.createGame(
                gameRequest(Arrays.asList(player1, player2))
        );

        RockPaperScissorsContract rockPaperScissorsContractForPlayer1 = new RockPaperScissorsContractImpl(
                testState,
                contractTransaction(player1, TxType.CALL_CONTRACT)
        );
        rockPaperScissorsContractForPlayer1.play(new PlayRequest(Util.hash(player1AnswerType + "_" + player1Salt)));


        RockPaperScissorsContract rockPaperScissorsContractForPlayer2 = new RockPaperScissorsContractImpl(
                testState,
                contractTransaction(player2, TxType.CALL_CONTRACT)
        );
        rockPaperScissorsContractForPlayer2.play(new PlayRequest(Util.hash(player2AnswerType + "_" + player2Salt)));

        rockPaperScissorsContractForPlayer1.reveal(new RevealRequest(player1Salt));
        rockPaperScissorsContractForPlayer2.reveal(new RevealRequest(player2Salt));

        GameStatus gameStatus = testState.get(GAME_STATUS_KEY, GameStatus.class);
        assertEquals(GameStatus.FINISHED, gameStatus);
        String winnerAddress = testState.get(GAME_WINNER_ADDR_KEY, String.class);
        assertEquals(player2.asBase58String(), winnerAddress);
    }

}