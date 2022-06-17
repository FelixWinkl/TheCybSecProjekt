import javax.net.ssl.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.cert.X509Certificate;

public class ServerMain
{
    /**
     * Server application entry point.
     */
    public static void main(String[] args)
    {
        // HACK to disable SSL validation (else Java refuses to connect with the main
        // server API)
        // From:
        // http://www.nakov.com/blog/2009/07/16/disable-certificate-validation-in-java-ssl-connections/
        try
        {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager()
            {
                public java.security.cert.X509Certificate[] getAcceptedIssuers()
                {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType)
                {
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType)
                {
                }
            }};

            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = (hostname, session) -> true;

            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        // Check lab mode
        if (!LabEnvironment.LAB_MODE)
            Utility.safePrintln("WARNING: The server application is currently *not* running in lab mode.\n    Communication with the lab server is disabled.");

        // Check parameters
        String argCommand = args[0];
        if ((argCommand.equalsIgnoreCase("run") && args.length < 4) || (argCommand.equalsIgnoreCase("generate") && args.length < 5))
        {
            // Show usage
            Utility.safePrintln("Usage:");
            Utility.safePrintln("    generate <database file> <mitm password file> <client configuration file> <attacker credentials file>");
            Utility.safePrintln("    run <database file> <ip> <port>");
            return;
        }

        // Generate new database?
        if (argCommand.equalsIgnoreCase("generate"))
        {
            // Create database file with given name and exit
            Database.generate(args[1], args[2], args[3], args[4]);
            Utility.safePrintln("Generating database file completed.");
            return;
        }
        else if (!argCommand.equalsIgnoreCase("run"))
        {
            Utility.safePrintln("Unknown command.");
            return;
        }

        // Parse remaining "run" parameters
        String ip = args[2];
        int port = Integer.parseInt(args[3]);

        // Read database
        Utility.safeDebugPrintln("Reading database file '" + args[1] + "'...");
        Database database = new Database(args[1]);

        // Create server socket
        Utility.safeDebugPrintln("Creating server socket...");
        try (ServerSocket serverSocket = new ServerSocket(port, 0, InetAddress.getByName(ip)))
        {
            // Listen for clients
            Utility.safeDebugPrintln("Enter client listen loop.");
            while (true)
            {
                // Accept new client
                Socket clientSocket = serverSocket.accept();
                Utility.safeDebugPrintln("Client accepted on port " + clientSocket.getLocalPort());

                // Start new thread to handle client
                new Thread(new ClientThread(clientSocket, database)).start();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}