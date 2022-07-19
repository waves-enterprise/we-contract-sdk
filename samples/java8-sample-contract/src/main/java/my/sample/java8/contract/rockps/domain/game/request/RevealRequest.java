package my.sample.java8.contract.rockps.domain.game.request;

public class RevealRequest {
    private String salt;

    public RevealRequest() {
    }

    public RevealRequest(String salt) {
        this.salt = salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getSalt() {
        return salt;
    }
}
