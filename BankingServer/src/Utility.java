import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 * Helper class containing auxiliary functions.
 */
public class Utility
{
    /**
     * Prints out the given message, while synchronizing between threads.
     * DO NOT CHANGE THIS METHOD.
     *
     * @param message The message to be printed.
     */
    public static void safePrintln(String message)
    {
        synchronized (System.out)
        {
            System.out.println(message);
        }

        // Store output additionally in debug file, if requested
        if (LabEnvironment.DEBUG_FILE != null)
        {
            synchronized (LabEnvironment.DEBUG_FILE)
            {
                try
                {
                    LabEnvironment.DEBUG_FILE.write("O: " + message + "\n");
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Prints out the given debug message, while synchronizing between threads.
     * DO NOT CHANGE THIS METHOD.
     *
     * @param message The message to be printed.
     */
    public static void safeDebugPrintln(String message)
    {
        // Dedicated debug output file?
        if (LabEnvironment.DEBUG_FILE != null)
        {
            synchronized (LabEnvironment.DEBUG_FILE)
            {
                try
                {
                    LabEnvironment.DEBUG_FILE.write("E: " + message + "\n");
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        else
        {
            synchronized (System.err)
            {
                // Do not show debug output in lab mode, to avoid spamming the container log
                if (LabEnvironment.LAB_MODE)
                    return;

                System.err.println(message);
            }
        }
    }

    /**
     * Writes the given string payload as a packet into the given output stream.
     *
     * @param outputStream The stream the packet shall be written to.
     * @param payload      The string payload to be sent.
     */
    public static void sendPacket(DataOutputStream outputStream, String payload) throws IOException
    {
        // Debug output
        safeDebugPrintln("Sending '" + payload + "'");

        // Convert payload to byte array
        byte[] payloadEncoded = payload.getBytes();

        // Validity check
        // DO NOT REMOVE THIS. If you hit this exception, you have been doing something wrong!
        // Putting non-ASCII characters in a String is dangerous and will break randomly, especially
        // if you split this string afterwards.
        for (byte b : payloadEncoded)
        {
            if (b < 0x20 || b >= 0x7f)
                throw new IOException("Can not send binary data as string.");
        }

        // Write packet length
        outputStream.writeInt(payloadEncoded.length);

        // Write payload
        outputStream.write(payloadEncoded);
    }

    /**
     * Writes the given binary payload as a packet into the given output stream.
     *
     * @param outputStream The stream the packet shall be written to.
     * @param payload      The binary payload to be sent.
     */
    public static void sendPacket(DataOutputStream outputStream, byte[] payload) throws IOException
    {
        // Debug output
        safeDebugPrintln("Sending '" + byteArrayToHex(payload) + "'");

        // Write packet length
        outputStream.writeInt(payload.length);

        // Write payload
        outputStream.write(payload);
    }

    /**
     * Receives the next packet from the given input stream.
     *
     * @param inputStream The stream where the packet shall be retrieved.
     * @return The payload of the received packet.
     */
    public static String receivePacket(DataInputStream inputStream) throws IOException
    {
        // Prepare payload buffer
        byte[] payloadEncoded = new byte[inputStream.readInt()];
        inputStream.readFully(payloadEncoded);

        // Decode payload
        String payload = new String(payloadEncoded);
        safeDebugPrintln("Received '" + payload + "'");
        return payload;
    }

    /**
     * Receives the next packet from the given input stream.
     *
     * @param inputStream The stream where the packet shall be retrieved.
     * @return The payload of the received packet.
     */
    public static byte[] receivePacketBinary(DataInputStream inputStream) throws IOException
    {
        // Read payload
        byte[] payload = new byte[inputStream.readInt()];
        inputStream.readFully(payload);

        safeDebugPrintln("Received '" + byteArrayToHex(payload) + "'");
        return payload;
    }

    /**
     * Returns a random alpha numeric string with the given length.
     *
     * @param length The length of the requested string.
     * @return A random alpha numeric string with the given length.
     */
    public static String getRandomString(int length)
    {
        // Generate random string efficiently
        int randomIndex = new Random().nextInt(100 - length);
        return "Sl4idafEVk9X1efZFSAUANyQefaua8JnnAVVQbhuEwrcA4c85yrMaaVjv1TiDbmPdQAD5pfyqcsj1obyEJxGulmaV8ezWYEXpyUs".substring(randomIndex, randomIndex + length);
    }

    /**
     * Converts a byte array into a hex string.
     * Intended for debugging output.
     *
     * @param array Byte array.
     * @return Hex string.
     */
    public static String byteArrayToHex(byte[] array)
    {
        StringBuilder stringBuilder = new StringBuilder(array.length * 3);

        boolean first = true;
        for (byte b : array)
        {
            if (first)
                first = false;
            else
                stringBuilder.append(" ");

            stringBuilder.append(String.format("%02x", b));
        }

        return stringBuilder.toString();
    }
}
