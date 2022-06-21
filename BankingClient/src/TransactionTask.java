import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Scanner;

/**
 * Sends money from the current user to another user.
 */
public class TransactionTask extends Task
{
    /**
     * A scanner object to read terminal input.
     */
    private final Scanner _terminalScanner;
    /**
     * Tells whether the transaction was successful.
     */
    private boolean _successful = false;

    /**
     * Creates a new transaction task.
     *
     * @param socketInputStream  The socket input stream.
     * @param socketOutputStream The socket output stream.
     * @param terminalScanner    A scanner object to read terminal input.
     */
    public TransactionTask(DataInputStream socketInputStream, DataOutputStream socketOutputStream, Scanner terminalScanner,  ClientConfiguration clientConfiguration)
    {
        // Call superclass constructor
        super(socketInputStream, socketOutputStream, clientConfiguration);

        // Save parameters
        _terminalScanner = terminalScanner;
    }

    /**
     * Executes a transaction.
     */
    public void run() throws IOException
    {
        // Read send parameters
        String recipient;
        int amount;
        Utility.safePrint("Recipient name: ");
        recipient = _terminalScanner.next();
        Utility.safePrint("Amount of money (1-10): ");
        amount = _terminalScanner.nextInt();

        // Inform server about transaction
        String prePacket = "transaction";
        Utility.safeDebugPrintln("Sending transaction header packet...");
        _communicator.sendPackage(_socketOutputStream, prePacket);

        // Send packet
        String transactionPacket = recipient + "," + amount;
        Utility.safeDebugPrintln("Sending transaction packet...");
        _communicator.sendPackage(_socketOutputStream, transactionPacket);

        // Wait for response packet
        String moneySendResponse = Utility.receivePacketNoEncryption(_socketInputStream);
        Utility.safeDebugPrintln("Server response: " + moneySendResponse);
        _successful = moneySendResponse.equals("Transaction successful.");
    }

    /**
     * Returns whether the transaction was successful.
     *
     * @return Whether the transaction was successful.
     */
    public boolean getSuccessful()
    {
        return _successful;
    }
}