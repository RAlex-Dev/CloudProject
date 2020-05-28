package pkg.model;

public class User {

    private String userName;
    private String password;
    private String code;
    private long credits;
    private boolean isBronze;
    private boolean isSilver;
    private boolean isGold;
    private String status;

    public User(String userName, String password, String code) {
        this.userName = userName;
        this.password = password;
        this.code = code;
        this.status = status;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getCode() {
        return code;
    }

    public long getCredits() {
        return credits;
    }

    public void setCredits(long credits) {
        this.credits = credits;
    }
}
