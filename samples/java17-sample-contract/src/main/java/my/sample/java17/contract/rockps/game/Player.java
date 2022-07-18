package my.sample.java17.contract.rockps.game;

public class Player {
    private String address;
    private String hashedAnswer;

    public Player() {
    }

    private AnswerType answer;

    public Player(String address, String hashedAnswer) {
        this.address = address;
        this.hashedAnswer = hashedAnswer;
    }

    public String getAddress() {
        return address;
    }

    public String getHashedAnswer() {
        return hashedAnswer;
    }

    public AnswerType getAnswer() {
        return answer;
    }

    public void setAnswer(AnswerType answer) {
        this.answer = answer;
    }
}
