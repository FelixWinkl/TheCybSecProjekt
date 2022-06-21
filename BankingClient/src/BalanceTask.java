import javax.crypto.SecretKey;
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
    public BalanceTask(DataInputStream socketInputStream, DataOutputStream socketOutputStream, ClientConfiguration clientConfiguration, SecretKey aesKey)
    {
        // Call superclass constructor
        super(socketInputStream, socketOutputStream, clientConfiguration, aesKey);
    }

    @Override
    public void run() throws IOException
    {
        // Send request packet
        String requestPacket = "balance";
        Utility.safeDebugPrintln("Sending balance request packet...");
        Utility.sendPacketAES(_socketOutputStream, requestPacket,_config.get_symmetricKey());

        // Read total money
        Utility.safeDebugPrintln("Waiting for first balance response packet...");
        String balanceMoneyResponse = Utility.receivePacketAES(_socketInputStream, _config.get_symmetricKey());
        int balanceMoney = Integer.parseInt(balanceMoneyResponse);
        Utility.safePrintln("Current money: " + balanceMoney);

        // Wait for count response packet
        Utility.safeDebugPrintln("Waiting for balance count packet...");
        String balanceCountResponse = Utility.receivePacketAES(_socketInputStream, _config.get_symmetricKey());
        int balanceCount = Integer.parseInt(balanceCountResponse);
        Utility.safeDebugPrintln("Balance entry count: " + balanceCount);

        // Read entries
        Utility.safeDebugPrintln("Reading balance entries...");
        Utility.safePrintln("Past transactions:");
        for (int i = 0; i < balanceCount; ++i)
        {
            // Receive & split entry data
            String balanceEntry = Utility.receivePacketAES(_socketInputStream,_config.get_symmetricKey());
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
