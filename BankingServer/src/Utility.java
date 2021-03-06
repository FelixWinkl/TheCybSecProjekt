import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
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
     * @param newline Controls whether a line break is inserted after the message.
     */
    private static void safePrint(String message, boolean newline)
    {
        synchronized (System.out)
        {
            if (newline)
                System.out.println(message);
            else
                System.out.print(message);
        }

        // Store output additionally in debug file, if requested
        if (LabEnvironment.DEBUG_FILE != null)
        {
            synchronized (LabEnvironment.DEBUG_FILE)
            {
                try
                {
                    LabEnvironment.DEBUG_FILE.write("O: " + message + "\n");
                    LabEnvironment.DEBUG_FILE.flush();
                }
                catch (IOException e)
                {
                    e.printStackTrace(); Utility.safeDebugPrintln("error: " +e.getMessage());
                }
            }
        }
    }

    /**
     * Prints out the given message, while synchronizing between threads.
     * DO NOT CHANGE THIS METHOD.
     *
     * @param message The message to be printed.
     */
    public static void safePrint(String message)
    {
        safePrint(message, false);
    }

    /**
     * Prints out the given message, while synchronizing between threads.
     * DO NOT CHANGE THIS METHOD.
     *
     * @param message The message to be printed.
     */
    public static void safePrintln(String message)
    {
        safePrint(message, true);
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
                    LabEnvironment.DEBUG_FILE.flush();
                }
                catch (IOException e)
                {
                    e.printStackTrace(); Utility.safeDebugPrintln("error: " +e.getMessage());
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
    private static void sendPacket(DataOutputStream outputStream, String payload) throws IOException
    {
        // Debug output
        safeDebugPrintln("Sending encrypted '" + payload + "'");

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
     * Writes the given string payload as a packet into the given output stream encrypted with AES.
     *
     * @param outputStream The stream the packet shall be written to.
     * @param payload      The string payload to be sent.
     */
    public static void sendPacketAES(DataOutputStream outputStream, String payload, SecretKey symmetricKey) throws IOException
    {
        safeDebugPrintln("Sending'" + payload + "'");
        IvParameterSpec iv =  AESHelper.generateIv();
        String encryptedPayload = AESHelper.encrypt(payload,symmetricKey,iv);
        encryptedPayload = AESHelper.ivToString(iv) + " " + encryptedPayload;
        sendPacket(outputStream, encryptedPayload);
    }

    /**
     * Sends package RSA encrypted.
     * @param socketOutputStream
     */
    public static void sendRSAPackage(DataOutputStream socketOutputStream, String message, PublicKey serverPublicKEy) throws IOException
    {
        String encryptedString = GenerateKeys.encryptMessage(message,serverPublicKEy);
        Utility.sendPacket(socketOutputStream, encryptedString);
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
    public static String receivePacketRSA(DataInputStream inputStream, PrivateKey privateKey) throws IOException
    {
        // Prepare payload buffer
        byte[] payloadEncoded = new byte[inputStream.readInt()];
        inputStream.readFully(payloadEncoded);

        // Decode payload
        String payload = new String(payloadEncoded);

        String encrypted = GenerateKeys.decryptMessage(payload,privateKey);

        safeDebugPrintln("Received '" + encrypted + "'");
        return encrypted;
    }
    /**
     * Receives the next packet from the given input stream.
     *
     * @param inputStream The stream where the packet shall be retrieved.
     * @return The payload of the received packet.
     */
    public static String receivePacketAES(DataInputStream inputStream, SecretKey symmetricKey) throws IOException
    {
        // Prepare payload buffer
        byte[] payloadEncoded = new byte[inputStream.readInt()];
        inputStream.readFully(payloadEncoded);

        // Decode payload
        String payload = new String(payloadEncoded);

        String ivString = payload.split(" ")[0];
        String msgString = payload.split(" ")[1];
        IvParameterSpec iv= AESHelper.stringToIv(ivString);
        String encrypted = AESHelper.decrypt(msgString,symmetricKey,iv);

        safeDebugPrintln("Received '" + encrypted + "'");
        return encrypted;
    }

    /*
    public static String receivePacketNoEncryption(DataInputStream inputStream) throws IOException
    {
        // Prepare payload buffer
        byte[] payloadEncoded = new byte[inputStream.readInt()];
        inputStream.readFully(payloadEncoded);


        // Decode payload
        String payload = new String(payloadEncoded);
        safeDebugPrintln("Received '" + payload + "'");
        return payload;
    }*/

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
