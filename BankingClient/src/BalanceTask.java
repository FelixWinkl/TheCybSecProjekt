import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Handles retrieval of the user's balance.
 */
public class BalanceTask extends Task
{
    /**
     * Creates a new balance retrieval task.
     *
     * @param socketInputStream  The socket input stream.
     * @param socketOutputStream The socket output stream.
     */
    public BalanceTask(DataInputStream socketInputStream, DataOutputStream socketOutputStream,  ClientConfiguration clientConfiguration)
    {
        // Call superclass constructor
        super(socketInputStream, socketOutputStream, clientConfiguration);
    }

    @Override
    public void run() throws IOException
    {
        // Send request packet
        String requestPacket = "balance";
        Utility.safeDebugPrintln("Sending balance request packet...");
        _communicator.sendPackage(_socketOutputStream, requestPacket);

        // Read total money
        Utility.safeDebugPrintln("Waiting for first balance response packet...");
        String balanceMoneyResponse = Utility.receivePacketNoEncryption(_socketInputStream);
        int balanceMoney = Integer.parseInt(balanceMoneyResponse);
        Utility.safePrintln("Current money: " + balanceMoney);

        // Wait for count response packet
        Utility.safeDebugPrintln("Waiting for balance count packet...");
        String balanceCountResponse = Utility.receivePacketNoEncryption(_socketInputStream);
        int balanceCount = Integer.parseInt(balanceCountResponse);
        Utility.safeDebugPrintln("Balance entry count: " + balanceCount);

        // Read entries
        Utility.safeDebugPrintln("Reading balance entries...");
        Utility.safePrintln("Past transactions:");
        for (int i = 0; i < balanceCount; ++i)
        {
            // Receive & split entry data
            String balanceEntry = Utility.receivePacketNoEncryption(_socketInputStream);
            String[] balanceEntryParts = balanceEntry.split(",");
            if (balanceEntryParts.length < 2)
            {
                Utility.safeDebugPrintln("Received invalid balance entry packet from server: " + balanceEntry);
                return;
            }
            String name = balanceEntryParts[0].trim();
            Integer amount = Integer.parseInt(balanceEntryParts[1].trim());

            // Print entry with appropriate whitespace padding
            // group   | 5
            // victim1 | -7
            System.out.printf("%-7s | %d%n", name, amount);
        }
    }

}
