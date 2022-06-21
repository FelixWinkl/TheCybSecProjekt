import javax.crypto.SecretKey;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ClientMain
{
    /**
     * Client application entry point.
     */
    public static void main(String[] args)
    {
        // Check parameters
        if (args.length < 4)
        {
            // Crash
            Utility.safePrintln("Please provide the client configuration file, the server's host name or IP address, its port and a directory for storing device codes.");
            return;
        }
        Utility.safeDebugPrintln("args1:" + args[0]);
        Utility.safeDebugPrintln("args2:" + args[1]);
        Utility.safeDebugPrintln("args3:" + args[2]);
        Utility.safeDebugPrintln("args4:" + args[3]);


        // Create scanner for terminal input
        Scanner terminalScanner = new Scanner(System.in);

        // Load client configuration
        ClientConfiguration clientConfiguration = new ClientConfiguration(args[0]);

        // Print implementation version
        // This value is hardcoded in the server, and automatically added to the compiled client
        Utility.safeDebugPrintln("clientConfigVersion: [ " + clientConfiguration.getVersion() + " ]");

        // Connect to server
        Utility.safePrintln("Connecting to server '" + args[1] + "' on port " + args[2]);
        try (Socket socket = new Socket(args[1], Integer.parseInt(args[2])))
        {
            // Get I/O streams
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());

            // Run login task
            LoginTask loginTask = new LoginTask(inputStream, outputStream, terminalScanner, clientConfiguration);
            loginTask.run();
            if (!loginTask.getSuccessful())
            {
                Utility.safePrintln("Login not successful, exiting...");
                return;
            }
            String userName = loginTask.getName();
            SecretKey symmetricAESKey = loginTask.getSymmetricKey();

            // Run until exit
            boolean deviceAuthenticated = false;
            while (true)
            {
                // Show action string
                Utility.safePrintln("What do you want to do?   View balance [b]   Do transaction [t]   Exit [e]");
                String action = terminalScanner.next();
                if (action.length() < 1)
                    continue;
                switch (action.charAt(0))
                {
                    case 'b' -> {
                        // Run balance retrieval task
                        Utility.safeDebugPrintln("Starting balance task...");
                        new BalanceTask(inputStream, outputStream, clientConfiguration,symmetricAESKey).run();
                    }
                    case 't' -> {
                        // Check for device authentication
                        if (!deviceAuthenticated)
                        {
                            // Run registration
                            Utility.safeDebugPrintln("Starting registration task...");
                            RegistrationTask registrationTask = new RegistrationTask(inputStream, outputStream, terminalScanner, userName, args[3], clientConfiguration, symmetricAESKey);
                            registrationTask.run();
                            if (!registrationTask.getSuccessful())
                                break;
                            deviceAuthenticated = true;
                        }

                        // Run transaction task
                        Utility.safeDebugPrintln("Starting transaction task...");
                        TransactionTask transactionTask = new TransactionTask(inputStream, outputStream, terminalScanner, clientConfiguration, symmetricAESKey);
                        transactionTask.run();

                        if (transactionTask.getSuccessful())
                            Utility.safePrintln("The transaction has been successful.");
                        else
                            Utility.safePrintln("The transaction has failed.");
                    }
                    case 'e' -> {
                        Utility.safePrintln("Terminating the connection...");
                        return;
                    }
                    default -> Utility.safePrintln("Unknown command.");
                }
            }
        }
        catch (IOException e)
        {
            Utility.safeDebugPrintln("error l99: " +e.getMessage());
        }
    }

}
