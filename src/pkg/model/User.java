package pkg.model;

public class User {

    private String userName;
    private String password;
    private String code;
    private long credits;
    private boolean isBronze;
    private boolean isSilver;
    private boolean isGold;

    public User(String userName, String password, String code) {
        this.userName = userName;
        this.password = password;
        this.code = code;
        this.credits = credits;
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

    public void setMemStat(long credits) {
        if (credits >= 500) {
            isBronze = true;
        }
        if (credits >= 1000) {
            isSilver = true;
        }
        if (credits >= 1500) {
            isGold = true;
        }
    }
}
