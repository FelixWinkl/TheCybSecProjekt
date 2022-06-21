import javax.crypto.SecretKey;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

/**
 * Sends login data to the server.
 */
public class LoginTask extends Task
{
    /**
     * A scanner object to read terminal input.
     */
    private final Scanner _terminalScanner;
    /**
     * Tells whether the login was successful.
     */
    private boolean _successful = false;
    /**
     * Contains the user name after successful login.
     */
    private String _name = "";

    /**
     * the newly created AED Key
     */
    SecretKey symmetricKey = null;

    /**
     * Creates a new login task.
     *
     * @param socketInputStream  The socket input stream.
     * @param socketOutputStream The socket output stream.
     * @param terminalScanner    A scanner object to read terminal input.
     */
    public LoginTask(DataInputStream socketInputStream, DataOutputStream socketOutputStream, Scanner terminalScanner,  ClientConfiguration clientConfiguration)
    {
        // Call superclass constructor
        super(socketInputStream, socketOutputStream, clientConfiguration, null);

        // Save parameters
        _terminalScanner = terminalScanner;
    }

    /**
     * Executes the login.
     */
    public void run() throws IOException
    {
        //symmetric Key vereinbaren

        try
        {
            symmetricKey = AESHelper.generateKey(128);
            String message = "OUR_KEY: " + AESHelper.secretKeyToString(symmetricKey);
            //System.out.println("login:"  + symmetricKey.toString());
            Utility.sendRSAPackage(_socketOutputStream, message, _config.get_serverPublicKey());
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }




        // Read credentials
        String password;
        Utility.safePrint("User: ");
        _name = _terminalScanner.next();
        Utility.safePrint("Password: ");
        password = _terminalScanner.next();

        // Send login packet
        String loginPacket = _name + "," + password;
        Utility.sendPacketAES(_socketOutputStream, loginPacket,symmetricKey);

        // Wait for response packet
        String loginResponse = Utility.receivePacketAES(_socketInputStream,symmetricKey);
        Utility.safeDebugPrintln("Server response: " + loginResponse);
        _successful = loginResponse.equals("Login OK.");
    }

    /**
     * Returns whether the login was successful.
     *
     * @return Whether the login was successful.
     */
    public boolean getSuccessful()
    {
        return _successful;
    }

    /**
     * Returns the user name.
     *
     * @return The user name.
     */
    public String getName()
    {
        return _name;
    }

    public SecretKey getSymmetricKey()
    {
        return symmetricKey;
    }
}
