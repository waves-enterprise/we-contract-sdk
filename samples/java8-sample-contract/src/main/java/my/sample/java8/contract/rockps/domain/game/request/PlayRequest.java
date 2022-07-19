package my.sample.java8.contract.rockps.domain.game.request;

public class PlayRequest {
    private String hashedAnswer;

    public PlayRequest() {
    }

    public PlayRequest(String hashedAnswer) {
        this.hashedAnswer = hashedAnswer;
    }

    public void setHashedAnswer(String hashedAnswer) {
        this.hashedAnswer = hashedAnswer;
    }

    public String getHashedAnswer() {
        return hashedAnswer;
    }
}
