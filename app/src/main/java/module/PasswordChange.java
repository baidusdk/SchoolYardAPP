package module;

public class PasswordChange {
    private String account;
    private String oldPassword;
    private String newPassword;

    public PasswordChange(String userAccount, String oldPassword, String newPassword) {
        this.account = userAccount;
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
