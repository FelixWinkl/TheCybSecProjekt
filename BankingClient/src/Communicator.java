import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.PublicKey;

/**
 * Handels all Communication
 */
public class Communicator
{
    /**
     * public Key of the Server
     */
    private PublicKey _serverPublicKey;


    public Communicator(PublicKey serverPublicKey)
    {
        _serverPublicKey = serverPublicKey;
    }

    /**
     * Send encryptedMessage
     * @param socketOutputStream
     */
    public void sendPackage(DataOutputStream socketOutputStream, String message) throws IOException
    {

        String encryptedString = GenerateKeys.encryptMessage(message,_serverPublicKey);
        Utility.sendPacket(socketOutputStream, encryptedString);
    }
}
