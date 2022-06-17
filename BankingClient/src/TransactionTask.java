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
    public TransactionTask(DataInputStream socketInputStream, DataOutputStream socketOutputStream, Scanner terminalScanner)
    {
        // Call superclass constructor
        super(socketInputStream, socketOutputStream);

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
        System.out.print("Recipient name: ");
        recipient = _terminalScanner.next();
        System.out.print("Amount of money (1-10): ");
        amount = _terminalScanner.nextInt();

        // Inform server about transaction
        String prePacket = "transaction";
        System.err.println("Sending transaction header packet...");
        Utility.sendPacket(_socketOutputStream, prePacket);

        // Send packet
        String transactionPacket = recipient + "," + amount;
        System.err.println("Sending transaction packet...");
        Utility.sendPacket(_socketOutputStream, transactionPacket);

        // Wait for response packet
        String moneySendResponse = Utility.receivePacket(_socketInputStream);
        System.err.println("Server response: " + moneySendResponse);
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