package my.sample.java17.contract.rockps.api;

import com.wavesenterprise.sdk.contract.api.annotation.ContractAction;
import com.wavesenterprise.sdk.contract.api.annotation.ContractInit;
import my.sample.java17.contract.rockps.game.request.CreateGameRequest;
import my.sample.java17.contract.rockps.game.request.PlayRequest;
import my.sample.java17.contract.rockps.game.request.RevealRequest;

public interface RockPaperScissorsContract {

    @ContractInit
    void createGame(CreateGameRequest createGameRequest);

    @ContractAction
    void play(PlayRequest registerPlayerRequest);

    @ContractAction
    void reveal(RevealRequest revealRequest);

    class Keys {
        public static final String PLAYER_ADDRESSES_KEY = "PLAYER_ADDRESSES";
        public static final String PLAYERS_MAPPING_PREFIX = "PLAYERS";

        public final static String GAME_KEY = "GAME";
        public static final String GAME_STATUS_KEY = "GAME_STATUS";
        public static final String GAME_WINNER_ADDR_KEY = "GAME_WINNER_ADDRESS";

        public static final String CREATED_DATE_KEY = "CREATED_DATE";
    }
}
