package Chat;

import java.io.Serializable;
import java.security.PublicKey;

public class RegistrationRequest implements Serializable {
    public String username;
    public PublicKey publicKey;

    RegistrationRequest(String username, PublicKey publicKey) {
        this.username = username;
        this.publicKey = publicKey;
    }
}
