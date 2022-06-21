import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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
     * Creates a new login task.
     *
     * @param socketInputStream  The socket input stream.
     * @param socketOutputStream The socket output stream.
     * @param terminalScanner    A scanner object to read terminal input.
     */
    public LoginTask(DataInputStream socketInputStream, DataOutputStream socketOutputStream, Scanner terminalScanner,  ClientConfiguration clientConfiguration)
    {
        // Call superclass constructor
        super(socketInputStream, socketOutputStream, clientConfiguration);

        // Save parameters
        _terminalScanner = terminalScanner;
    }

    /**
     * Executes the login.
     */
    public void run() throws IOException
    {
        // Read credentials
        _communicator.sendPackage(_socketOutputStream, loginPacket);


        String password;
        Utility.safePrint("User: ");
        _name = _terminalScanner.next();
        Utility.safePrint("Password: ");
        password = _terminalScanner.next();

        // Send login packet
        String loginPacket = _name + "," + password;
        _communicator.sendPackage(_socketOutputStream, loginPacket);

        // Wait for response packet
        String loginResponse = Utility.receivePacketNoEncryption(_socketInputStream);
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
}
