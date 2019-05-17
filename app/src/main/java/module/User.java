package module;

import java.io.Serializable;

public class User implements Serializable {
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
