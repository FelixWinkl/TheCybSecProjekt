import javax.crypto.SecretKey;
import java.security.PublicKey;

/**
 * Handels all Communication
 */
public class Config
{
    /**
     * public Key of the Server
     */
    private PublicKey _serverPublicKey;

    private SecretKey _symmetricKey;

    public PublicKey get_serverPublicKey()
    {
        return _serverPublicKey;
    }

    public SecretKey get_symmetricKey()
    {
        return _symmetricKey;
    }

    public Config(PublicKey serverPublicKey, SecretKey symmetricKey)
    {
        _serverPublicKey = serverPublicKey;
        _symmetricKey = symmetricKey;
    }

}
