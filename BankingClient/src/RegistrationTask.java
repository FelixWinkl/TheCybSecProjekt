import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Registers the current device on the server.
 */
public class RegistrationTask extends Task
{
    /**
     * A scanner object to read terminal input.
     */
    private final Scanner _terminalScanner;
    /**
     * The name of the user currently logged in.
     */
    private final String _userName;
    /**
     * Prefix for the device code file path.
     */
    private final String _deviceCodeFilePathPrefix;
    /**
     * Tells whether the transaction was successful.
     */
    private boolean _successful = false;

    /**
     * Creates a registration task.
     *
     * @param socketInputStream        The socket input stream.
     * @param socketOutputStream       The socket output stream.
     * @param terminalScanner          A scanner object to read terminal input.
     * @param userName                 The name of the user currently logged in.
     * @param deviceCodeFilePathPrefix Base path of the device code file (default is
     *                                 the working directory).
     */
    public RegistrationTask(DataInputStream socketInputStream, DataOutputStream socketOutputStream, Scanner terminalScanner, String userName, String deviceCodeFilePathPrefix,  ClientConfiguration clientConfiguration)
    {
        // Call superclass constructor
        super(socketInputStream, socketOutputStream,  clientConfiguration);

        // Save parameters
        _terminalScanner = terminalScanner;
        _userName = userName;
        _deviceCodeFilePathPrefix = deviceCodeFilePathPrefix;
    }

    /**
     * Executes the registration.
     */
    public void run() throws IOException
    {
        // Check whether an device code has been generated in the past
        Path deviceCodeFilename = Paths.get(_deviceCodeFilePathPrefix, "banking_device_" + _userName + ".txt");
        if (new File(deviceCodeFilename.toString()).exists())
        {
            // Read device authentication code from file
            Utility.safeDebugPrintln("Authentication file detected, reading device code...");
            String authenticationCode;
            try (Scanner deviceCodeFileScanner = new Scanner(new FileReader(deviceCodeFilename.toString())))
            {
                authenticationCode = deviceCodeFileScanner.next();
            }
            catch (IOException e)
            {
                Utility.safeDebugPrintln("error l67: " +e.getMessage());
                return;
            }

            // Inform server about authentication
            String prePacket = "authentication";
            Utility.safeDebugPrintln("Sending authentication header packet...");
            _communicator.sendPackage(_socketOutputStream, prePacket);

            // Send authentication code
            Utility.safeDebugPrintln("Sending authentication code...");
            _communicator.sendPackage(_socketOutputStream, authenticationCode);

            // Wait for confirmation by server
            Utility.safeDebugPrintln("Waiting for server confirmation...");
            String serverConfirmation = Utility.receivePacketNoEncryption(_socketInputStream);
            Utility.safeDebugPrintln("Server response: " + serverConfirmation);
            if (!serverConfirmation.equals("Authentication successful."))
            {
                // Show error
                Utility.safePrintln("Authentication failed. Maybe the device code file is too old or invalid?");
                return;
            }
        }
        else
        {
            // Inform server about registration
            String prePacket = "registration";
            Utility.safeDebugPrintln("Sending registration header packet...");
            _communicator.sendPackage(_socketOutputStream, prePacket);

            // Generate half of registration code
            Utility.safeDebugPrintln("Generating and sending registration code part 1/2...");
            String registrationCodePart1 = Utility.getRandomString(4);
            _communicator.sendPackage(_socketOutputStream, registrationCodePart1);

            // Receive other half of registration code from server
            Utility.safeDebugPrintln("Waiting for registration code part 2/2...");
            String registrationCodePart2 = Utility.receivePacketNoEncryption(_socketInputStream);
            if (registrationCodePart2.length() != 4)
            {
                // Output response and stop registration process
                Utility.safeDebugPrintln("Received invalid registration code part from server: " + registrationCodePart2);
                return;
            }
            String registrationCode = registrationCodePart1 + registrationCodePart2;
            Utility.safeDebugPrintln("Received full registration code.");

            // Read confirmation code that the server should have sent via email
            Utility.safePrint("Confirmation code: ");
            String confirmationCode = _terminalScanner.next();

            // Send confirmation code
            Utility.safeDebugPrintln("Sending confirmation code...");
            _communicator.sendPackage(_socketOutputStream, confirmationCode);

            // Wait for confirmation by server
            Utility.safeDebugPrintln("Waiting for server confirmation...");
            String serverConfirmation = Utility.receivePacketNoEncryption(_socketInputStream);
            Utility.safeDebugPrintln("Server response: " + serverConfirmation);
            if (!serverConfirmation.equals("Registration successful."))
                return;

            // Save registration code
            try (FileWriter deviceCodeFileWriter = new FileWriter(deviceCodeFilename.toString()))
            {
                deviceCodeFileWriter.write(registrationCode);
            }
            catch (IOException e)
            {
                Utility.safeDebugPrintln("error l137: " +e.getMessage());
                return;
            }
        }
        _successful = true;
    }

    /**
     * Returns whether the registration was successful.
     *
     * @return Whether the registration was successful.
     */
    public boolean getSuccessful()
    {
        return _successful;
    }
}