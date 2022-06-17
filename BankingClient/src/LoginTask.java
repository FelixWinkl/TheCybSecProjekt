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
    public LoginTask(DataInputStream socketInputStream, DataOutputStream socketOutputStream, Scanner terminalScanner)
    {
        // Call superclass constructor
        super(socketInputStream, socketOutputStream);

        // Save parameters
        _terminalScanner = terminalScanner;
    }

    /**
     * Executes the login.
     */
    public void run() throws IOException
    {
        // Read credentials
        String password;
        System.out.print("User: ");
        _name = _terminalScanner.next();
        System.out.print("Password: ");
        password = _terminalScanner.next();

        // Send login packet
        String loginPacket = _name + "," + password;
        Utility.sendPacket(_socketOutputStream, loginPacket);

        // Wait for response packet
        String loginResponse = Utility.receivePacket(_socketInputStream);
        System.err.println("Server response: " + loginResponse);
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
