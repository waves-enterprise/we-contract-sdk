package my.sample.java8.contract.rockps.domain.game;

import my.sample.java8.contract.rockps.util.Util;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Game {
    private Map<String, Player> players;
    private Player winner;
    private GameStatus status = GameStatus.ACTIVE;

    public Game() {
    }

    public Map<String, Player> getPlayers() {
        return players;
    }

    public Player getWinner() {
        return winner;
    }

    public Game(List<Player> players) {
        this.players = players.stream().collect(Collectors.toMap(Player::getAddress, Function.identity()));
    }

    public void reveal(String address, String salt) {
        Player player = players.get(address);
        if (player == null) {
            throw new IllegalArgumentException("Address " + address + " isn't among players of the game");
        }
        Map<String, AnswerType> hashedAnswers = getHashedAnswers(salt);
        AnswerType answer = hashedAnswers.get(player.getHashedAnswer());
        if (answer == null) {
            throw new IllegalArgumentException("Not found matching answer for salt");
        }
        player.setAnswer(answer);
        if (allPlayersAnswered()) {
            finish();
        }
    }

    private void finish() {
        status = GameStatus.FINISHED;
        if (allPlayersHaveTheSameAnswers()) {
            return;
        }
        this.winner = players.values().stream().max(
                (player1, player2) -> new AnswerComparator().compare(player1.getAnswer(), player2.getAnswer())
        ).orElseThrow(() -> new IllegalStateException("Winner could not be determined"));
    }

    private boolean allPlayersHaveTheSameAnswers() {
        Set<AnswerType> differentAnswers = players.values().stream().map(Player::getAnswer).collect(Collectors.toSet());
        return differentAnswers.size() == 1;
    }

    private boolean allPlayersAnswered() {
        return players.values().stream().allMatch(player -> player.getAnswer() != null);
    }

    public Map<String, AnswerType> getHashedAnswers(String salt) {
        return Arrays.stream(AnswerType.values()).collect(
                Collectors.toMap(answerType -> Util.hash(answerType + "_" + salt), Function.identity())
        );
    }

    public GameStatus getStatus() {
        return status;
    }

    static class AnswerComparator implements Comparator<AnswerType> {

        @Override
        public int compare(AnswerType o1, AnswerType o2) {
            if ((o1 == AnswerType.SCISSORS && o2 == AnswerType.ROCK) || (o1 == AnswerType.ROCK && o2 == AnswerType.SCISSORS)) {
                return -o1.compareTo(o2);
            }
            return o1.compareTo(o2);
        }
    }
}