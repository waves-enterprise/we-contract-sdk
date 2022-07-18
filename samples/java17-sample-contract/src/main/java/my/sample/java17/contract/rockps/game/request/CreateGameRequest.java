package my.sample.java17.contract.rockps.game.request;

import java.util.List;

public record CreateGameRequest(List<Player> players) {

    public static record Player(String address) {
    }
}
