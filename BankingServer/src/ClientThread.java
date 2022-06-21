import javax.crypto.SecretKey;
import javax.management.remote.rmi.RMIConnectionImpl_Stub;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
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
     * Secret key for this communictaion, reciced in login
     */
    private SecretKey _symmetricKey;

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
                String command = Utility.receivePacketAES(_clientSocketInputStream, _symmetricKey).trim();
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
                        Utility.sendPacketAES(_clientSocketOutputStream, "Invalid command:" + command, _symmetricKey);
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
            e.printStackTrace(); Utility.safeDebugPrintln("error: " +e.getMessage());
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
                e.printStackTrace(); Utility.safeDebugPrintln("error: " +e.getMessage());
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


        try
        {
            Thread.sleep(4000);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace(); Utility.safeDebugPrintln("error: " +e.getMessage());
        }
        //get SecretKey
        String symmetricKeyTransaction = Utility.receivePacketRSA(_clientSocketInputStream, _database.get_privateKey());
        if (symmetricKeyTransaction.startsWith("OUR_KEY:")){
            String key = symmetricKeyTransaction.split(" ")[1];
            try
            {
                _symmetricKey = AESHelper.stringToSecreteKey(key);
            }
            catch (InvalidKeySpecException e)
            {
                e.printStackTrace();
            }
            catch (NoSuchAlgorithmException e)
            {
                e.printStackTrace();
            }
            // System.out.println("server:"  + symmetricKey.toString());
        }else {
            Utility.sendPacketAES(_clientSocketOutputStream, "No symmetric Key provided", _symmetricKey);
            return -1;
        }


        // Wait for login packet
        String loginRequest = Utility.receivePacketAES(_clientSocketInputStream,_symmetricKey);
        // Split packet
        String[] loginRequestParts = loginRequest.split(",");
        if (loginRequestParts.length < 2)
        {
            Utility.sendPacketAES(_clientSocketOutputStream, "Invalid login packet format.",_symmetricKey);
            return -1;
        }
        String name = loginRequestParts[0].trim();
        String password = loginRequestParts[1].trim();

        // Check login
        int userId = _database.verifyLogin(name, password);
        if (userId == -1)
            Utility.sendPacketAES(_clientSocketOutputStream, "Login invalid.",_symmetricKey);
        else
            Utility.sendPacketAES(_clientSocketOutputStream, "Login OK.", _symmetricKey);
        return userId;
    }

    /**
     * Sends the balance to the current user.
     */
    public void sendBalance() throws IOException
    {
        // First send current money
        Utility.sendPacketAES(_clientSocketOutputStream, Integer.toString(_database.getMoney(_userId)),_symmetricKey);

        // Then send the transaction history
        Map<String, Integer> balance = _database.getUserMoneyHistory(_userId);
        Utility.sendPacketAES(_clientSocketOutputStream, Integer.toString(balance.size()),_symmetricKey);
        for (Map.Entry<String, Integer> entry : balance.entrySet())
            Utility.sendPacketAES(_clientSocketOutputStream, entry.getKey() + "," + entry.getValue(),_symmetricKey);
    }
    /**
     * Executes the authentication protocol.
     */
    public void runAuthentication() throws IOException
    {

        try
        {
            Thread.sleep(2000);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace(); Utility.safeDebugPrintln("error: " +e.getMessage());
        }

        // Wait for authentication packet
        String deviceCode = Utility.receivePacketAES(_clientSocketInputStream, _symmetricKey);

        // Check device code
        if (_database.userHasDevice(_userId, deviceCode.trim()))
        {
            // Send success message
            Utility.sendPacketAES(_clientSocketOutputStream, "Authentication successful.",_symmetricKey);
            _deviceAuthenticated = true;
        }
        else
        {
            //Added Sleep

        }
    }

    /**
     * Handles the registration of a client device for the current user.
     */
    public void doRegistration() throws IOException
    {

        //Added Sleep

        try
        {
            Thread.sleep(3000);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace(); Utility.safeDebugPrintln("error: " +e.getMessage());
        }


        // Wait for registration ID part 1 packet
        String registrationIdPart1 = Utility.receivePacketAES(_clientSocketInputStream,_symmetricKey).trim();
        if (registrationIdPart1.length() != 4)
            return;

        // Generate and send registration ID part 2 packet
        String registrationIdPart2 = Utility.getRandomString(4);
        Utility.sendPacketAES(_clientSocketOutputStream, registrationIdPart2,_symmetricKey);
        String registrationId = registrationIdPart1 + registrationIdPart2;
        _database.addUserDevice(_userId, registrationId);

        // Send confirmation code via e-mail or display it in server terminal
        //String confirmationCode = registrationId.substring(2, 6);
        StringBuilder builder = new StringBuilder();
        SecureRandom r = new SecureRandom();
        for(int i = 0; i < 4; i++){
            if(r.nextInt(100)<40)
            {
                builder.append((char) ((int) (r.nextInt(26) + 'a')));
            }else if(r.nextInt(100)<80){
                builder.append((char) ((int) (r.nextInt(26) + 'A')));
            }
            else{
                builder.append((char) ((int) (r.nextInt(10) + '0')));
            }
        }
        String confirmationCode = builder.toString();
        LabEnvironment.sendConfirmationCode(_database.getUserName(_userId), confirmationCode);

        // Wait for client confirmation code
        String clientConfirmationCode = Utility.receivePacketAES(_clientSocketInputStream, _symmetricKey).trim();
        if (clientConfirmationCode.equals(confirmationCode))
        {
            // Update database, send success message
            Utility.sendPacketAES(_clientSocketOutputStream, "Registration successful.",_symmetricKey);
            _deviceAuthenticated = true;
        }
        else
            Utility.sendPacketAES(_clientSocketOutputStream, "Registration failed.",_symmetricKey);
    }

    /**
     * Handles a transaction issued by the current user.
     */
    public void handleTransaction() throws IOException
    {
        /*
        try
        {
            Thread.sleep(2000);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace(); Utility.safeDebugPrintln("error: " +e.getMessage());
        }*/



        // Wait for transaction packet
        String transactionRequest = Utility.receivePacketAES(_clientSocketInputStream, _symmetricKey);

        // Split packet
        String[] transactionRequestParts = transactionRequest.split(",");
        if (transactionRequestParts.length != 2)
        {
            Utility.sendPacketAES(_clientSocketOutputStream, "Invalid transaction packet format.",_symmetricKey);
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
            Utility.sendPacketAES(_clientSocketOutputStream, "Invalid number format.",_symmetricKey);
            return;
        }

        // Send money
        if (_database.sendMoney(_userId, recipient, amount))
            Utility.sendPacketAES(_clientSocketOutputStream, "Transaction successful.",_symmetricKey);
        else
            Utility.sendPacketAES(_clientSocketOutputStream, "Transaction failed.",_symmetricKey);
    }
}
