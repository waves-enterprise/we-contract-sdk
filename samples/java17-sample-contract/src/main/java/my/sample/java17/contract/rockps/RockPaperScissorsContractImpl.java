package my.sample.java17.contract.rockps;

import com.wavesenterprise.sdk.contract.api.annotation.ContractHandler;
import com.wavesenterprise.sdk.contract.api.state.ContractState;
import com.wavesenterprise.sdk.contract.api.state.TypeReference;
import com.wavesenterprise.sdk.contract.api.state.mapping.Mapping;
import com.wavesenterprise.sdk.node.domain.contract.ContractTransaction;
import my.sample.java17.contract.rockps.api.RockPaperScissorsContract;
import my.sample.java17.contract.rockps.game.Game;
import my.sample.java17.contract.rockps.game.GameStatus;
import my.sample.java17.contract.rockps.game.Player;
import my.sample.java17.contract.rockps.game.request.CreateGameRequest;
import my.sample.java17.contract.rockps.game.request.PlayRequest;
import my.sample.java17.contract.rockps.game.request.RevealRequest;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

import static my.sample.java17.contract.rockps.api.RockPaperScissorsContract.Keys.*;

@ContractHandler
public class RockPaperScissorsContractImpl implements RockPaperScissorsContract {

    private final ContractState contractState;
    private final ContractTransaction tx;

    private final Mapping<Player> players;

    public RockPaperScissorsContractImpl(ContractState contractState, ContractTransaction tx) {
        this.tx = tx;
        this.contractState = contractState;
        this.players = contractState.getMapping(Player.class, PLAYERS_MAPPING_PREFIX);
    }

    public void createGame(CreateGameRequest createGameRequest) {
        if (createGameRequest.players().size() > 2) {
            throw new IllegalArgumentException("Currently only two players are supported");
        }
        contractState.put(CREATED_DATE_KEY, txTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        contractState.put(
                PLAYER_ADDRESSES_KEY,
                createGameRequest.players().stream().map(CreateGameRequest.Player::address).collect(Collectors.toSet())
        );
    }

    public void play(PlayRequest registerPlayerRequest) {
        if (contractState.tryGet(GAME_KEY, Game.class).isPresent()) {
            throw new IllegalStateException("Game has already started");
        }
        String senderAddress = senderAddress();
        Set<String> addresses = getPlayerAddresses();
        String txSenderAddress = txSender();
        if (!addresses.contains(txSenderAddress)) {
            throw new IllegalAccessError("Address " + txSenderAddress + " is not present in the players of this game");
        }
        players.put(senderAddress(), new Player(senderAddress, registerPlayerRequest.hashedAnswer()));
        if (players.hasAll(addresses)) {
            startGame(addresses);
        }
    }

    public void reveal(RevealRequest revealRequest) {
        Game game = contractState.tryGet(GAME_KEY, Game.class)
                .orElseThrow(() -> new IllegalStateException("Could not reveal my result for not active game"));
        if (game.getStatus() == GameStatus.FINISHED) {
            throw new IllegalStateException("Game has already finished");
        }
        String currentAddress = txSender();
        game.reveal(currentAddress, revealRequest.salt());
        players.put(currentAddress, game.getPlayers().get(currentAddress));
        contractState.put(GAME_KEY, game);
        contractState.put(GAME_STATUS_KEY, game.getStatus());
        if (game.getWinner() != null) {
            contractState.put(GAME_WINNER_ADDR_KEY, game.getWinner().getAddress());
        }
    }

    private void startGame(Set<String> addresses) {
        Game game = new Game(new ArrayList<>(players.getAll(addresses).values()));
        contractState.put(GAME_KEY, game);
        contractState.put(GAME_STATUS_KEY, game.getStatus());
    }

    private Set<String> getPlayerAddresses() {
        return contractState.get(PLAYER_ADDRESSES_KEY, new TypeReference<>() {
        });
    }

    @NotNull
    private String txSender() {
        return tx.getSender().asBase58String();
    }

    private OffsetDateTime txTimestamp() {
        return OffsetDateTime.ofInstant(
                Instant.ofEpochMilli(tx.getTimestamp().getUtcTimestampMillis()),
                ZoneId.of("UTC")
        );
    }

    private String senderAddress() {
        return txSender();
    }
}
