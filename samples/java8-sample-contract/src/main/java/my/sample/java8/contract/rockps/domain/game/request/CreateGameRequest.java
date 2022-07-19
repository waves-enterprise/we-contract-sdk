package my.sample.java8.contract.rockps.domain.game.request;

import java.util.List;

public class CreateGameRequest {
    private List<Player> players;

    public CreateGameRequest() {
    }

    public CreateGameRequest(List<Player> players) {
        this.players = players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public static class Player {
        private String address;

        public Player() {
        }

        public Player(String address) {
            this.address = address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getAddress() {
            return address;
        }
    }
}
