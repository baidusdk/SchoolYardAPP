package module;

public class User {
        private String account;
        private String password;

        public void User(String tel, String passwd) {
            this.account = tel;
            this.password = passwd;
        }

        public String getAccount() {
            return account;
        }

        public void setAccount(String account){
            this.account = account;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
}
