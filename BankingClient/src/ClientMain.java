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
            System.out.println("Please provide the client configuration file, the server's host name or IP address, its port and a directory for storing device codes.");
            return;
        }

        // Create scanner for terminal input
        Scanner terminalScanner = new Scanner(System.in);

        // Load client configuration
        ClientConfiguration clientConfiguration = new ClientConfiguration(args[0]);

        // Print implementation version
        // This value is hardcoded in the server, and automatically added to the compiled client
        System.err.println("[ " + clientConfiguration.getVersion() + " ]");

        // Connect to server
        System.out.println("Connecting to server '" + args[1] + "' on port " + args[2]);
        try (Socket socket = new Socket(args[1], Integer.parseInt(args[2])))
        {
            // Get I/O streams
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());

            // Run login task
            LoginTask loginTask = new LoginTask(inputStream, outputStream, terminalScanner);
            loginTask.run();
            if (!loginTask.getSuccessful())
            {
                System.out.println("Login not successful, exiting...");
                return;
            }
            String userName = loginTask.getName();

            // Run until exit
            boolean deviceAuthenticated = false;
            while (true)
            {
                // Show action string
                System.out.println("What do you want to do?   View balance [b]   Do transaction [t]   Exit [e]");
                String action = terminalScanner.next();
                if (action.length() < 1)
                    continue;
                switch (action.charAt(0))
                {
                    case 'b' -> {
                        // Run balance retrieval task
                        System.err.println("Starting balance task...");
                        new BalanceTask(inputStream, outputStream).run();
                    }
                    case 't' -> {
                        // Check for device authentication
                        if (!deviceAuthenticated)
                        {
                            // Run registration
                            System.err.println("Starting registration task...");
                            RegistrationTask registrationTask = new RegistrationTask(inputStream, outputStream, terminalScanner, userName, args[3]);
                            registrationTask.run();
                            if (!registrationTask.getSuccessful())
                                break;
                            deviceAuthenticated = true;
                        }

                        // Run transaction task
                        System.err.println("Starting transaction task...");
                        TransactionTask transactionTask = new TransactionTask(inputStream, outputStream, terminalScanner);
                        transactionTask.run();

                        if (transactionTask.getSuccessful())
                            System.out.println("The transaction has been successful.");
                        else
                            System.out.println("The transaction has failed.");
                    }
                    case 'e' -> {
                        System.out.println("Terminating the connection...");
                        return;
                    }
                    default -> System.out.println("Unknown command.");
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

}
