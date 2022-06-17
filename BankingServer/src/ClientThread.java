import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;

/**
 * Handles a client connection.
 */
public class ClientThread implements Runnable
{
    /**
     * The underlying client socket.
     */
    private final Socket _clientSocket;

    /**
     * The input stream of the client socket.
     */
    private DataInputStream _clientSocketInputStream;

    /**
     * The output stream of the client socket.
     */
    private DataOutputStream _clientSocketOutputStream;

    /**
     * The database containing user data.
     */
    private final Database _database;

    /**
     * The ID of the user that logged in.
     */
    private int _userId = -1;

    /**
     * Determines whether the client device has been authenticated.
     */
    private boolean _deviceAuthenticated = false;

    /**
     * Creates a new thread that processes the given client socket.
     *
     * @param clientSocket The socket of the new client.
     * @param database     The database containing user data.
     */
    public ClientThread(Socket clientSocket, Database database)
    {
        // Save parameters
        _clientSocket = clientSocket;
        _database = database;
    }

    /**
     * The thread entry point.
     */
    @Override
    public void run()
    {
        Utility.safeDebugPrintln("Client thread started on port " + _clientSocket.getLocalPort() + ".");
        try
        {
            // Get send and receive streams
            _clientSocketInputStream = new DataInputStream(_clientSocket.getInputStream());
            _clientSocketOutputStream = new DataOutputStream(_clientSocket.getOutputStream());

            // Repeat login protocol until login is valid
            do
                _userId = runLogin();
            while (_userId == -1);
            Utility.safeDebugPrintln("User " + _userId + " logged in.");

            // Run until connection is closed
            while (!_clientSocket.isClosed())
            {
                // Check for commands
                String command = Utility.receivePacket(_clientSocketInputStream).trim();
                Utility.safeDebugPrintln("User " + _userId + " sent command '" + command + "'.");
                switch (command)
                {
                    case "balance":
                    {
                        sendBalance();
                        break;
                    }

                    case "authentication":
                    {
                        runAuthentication();
                        if (_deviceAuthenticated)
                            Utility.safeDebugPrintln("User " + _userId + " successfully authenticated.");
                        break;
                    }

                    case "registration":
                    {
                        doRegistration();
                        if (_deviceAuthenticated)
                            Utility.safeDebugPrintln("User " + _userId + " successfully registered a new device and authenticated.");
                        break;
                    }

                    case "transaction":
                    {
                        if (!_deviceAuthenticated)
                            Utility.safeDebugPrintln("User " + _userId + " requested transaction without device authentication.");
                        else
                            handleTransaction();
                        break;
                    }

                    default:
                    {
                        // This command does not exist, notify client
                        Utility.safeDebugPrintln("Command is invalid.");
                        Utility.sendPacket(_clientSocketOutputStream, "Invalid command:" + command);
                        break;
                    }
                }
            }
        }
        catch (EOFException e)
        {
            // Socket was closed
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            Utility.safeDebugPrintln("Doing cleanup...");
            try
            {
                // Clean up resources
                _clientSocketInputStream.close();
                _clientSocketOutputStream.close();
                _clientSocket.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            Utility.safeDebugPrintln("Cleanup complete.");
        }
    }

    /**
     * Executes the login protocol and returns the ID of the user.
     *
     * @return The ID of the user that logged in.
     */
    public int runLogin() throws IOException
    {
        // Wait for login packet
        String loginRequest = Utility.receivePacket(_clientSocketInputStream);

        // Split packet
        String[] loginRequestParts = loginRequest.split(",");
        if (loginRequestParts.length < 2)
        {
            Utility.sendPacket(_clientSocketOutputStream, "Invalid login packet format.");
            return -1;
        }
        String name = loginRequestParts[0].trim();
        String password = loginRequestParts[1].trim();

        // Check login
        int userId = _database.verifyLogin(name, password);
        if (userId == -1)
            Utility.sendPacket(_clientSocketOutputStream, "Login invalid.");
        else
            Utility.sendPacket(_clientSocketOutputStream, "Login OK.");
        return userId;
    }

    /**
     * Sends the balance to the current user.
     */
    public void sendBalance() throws IOException
    {
        // First send current money
        Utility.sendPacket(_clientSocketOutputStream, Integer.toString(_database.getMoney(_userId)));

        // Then send the transaction history
        Map<String, Integer> balance = _database.getUserMoneyHistory(_userId);
        Utility.sendPacket(_clientSocketOutputStream, Integer.toString(balance.size()));
        for (Map.Entry<String, Integer> entry : balance.entrySet())
            Utility.sendPacket(_clientSocketOutputStream, entry.getKey() + "," + entry.getValue());
    }

    /**
     * Executes the authentication protocol.
     */
    public void runAuthentication() throws IOException
    {
        // Wait for authentication packet
        String deviceCode = Utility.receivePacket(_clientSocketInputStream);

        // Check device code
        if (_database.userHasDevice(_userId, deviceCode.trim()))
        {
            // Send success message
            Utility.sendPacket(_clientSocketOutputStream, "Authentication successful.");
            _deviceAuthenticated = true;
        }
        else
            Utility.sendPacket(_clientSocketOutputStream, "Authentication failed.");
    }

    /**
     * Handles the registration of a client device for the current user.
     */
    public void doRegistration() throws IOException
    {
        // Wait for registration ID part 1 packet
        String registrationIdPart1 = Utility.receivePacket(_clientSocketInputStream).trim();
        if (registrationIdPart1.length() != 4)
            return;

        // Generate and send registration ID part 2 packet
        String registrationIdPart2 = Utility.getRandomString(4);
        Utility.sendPacket(_clientSocketOutputStream, registrationIdPart2);
        String registrationId = registrationIdPart1 + registrationIdPart2;
        _database.addUserDevice(_userId, registrationId);

        // Send confirmation code via e-mail or display it in server terminal
        String confirmationCode = registrationId.substring(2, 6);
        LabEnvironment.sendConfirmationCode(_database.getUserName(_userId), confirmationCode);

        // Wait for client confirmation code
        String clientConfirmationCode = Utility.receivePacket(_clientSocketInputStream).trim();
        if (clientConfirmationCode.equals(confirmationCode))
        {
            // Update database, send success message
            Utility.sendPacket(_clientSocketOutputStream, "Registration successful.");
            _deviceAuthenticated = true;
        }
        else
            Utility.sendPacket(_clientSocketOutputStream, "Registration failed.");
    }

    /**
     * Handles a transaction issued by the current user.
     */
    public void handleTransaction() throws IOException
    {
        // Wait for transaction packet
        String transactionRequest = Utility.receivePacket(_clientSocketInputStream);

        // Split packet
        String[] transactionRequestParts = transactionRequest.split(",");
        if (transactionRequestParts.length != 2)
        {
            Utility.sendPacket(_clientSocketOutputStream, "Invalid transaction packet format.");
            return;
        }
        String recipient = transactionRequestParts[0].trim();

        // Parse and check money amount parameter
        int amount;
        try
        {
            // Parse
            amount = Integer.parseInt(transactionRequestParts[1].trim());

            // Check range
            if (amount < 0 || amount > 10)
                amount = 10;
        }
        catch (NumberFormatException e)
        {
            Utility.sendPacket(_clientSocketOutputStream, "Invalid number format.");
            return;
        }

        // Send money
        if (_database.sendMoney(_userId, recipient, amount))
            Utility.sendPacket(_clientSocketOutputStream, "Transaction successful.");
        else
            Utility.sendPacket(_clientSocketOutputStream, "Transaction failed.");
    }
}
