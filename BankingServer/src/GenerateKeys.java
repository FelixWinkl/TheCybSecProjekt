import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class GenerateKeys {

    private KeyPairGenerator keyGen;
    private KeyPair pair;
    private PrivateKey privateKey;
    private PublicKey publicKey;

    public GenerateKeys(int keylength) throws NoSuchAlgorithmException, NoSuchProviderException {
        this.keyGen = KeyPairGenerator.getInstance("RSA");
        this.keyGen.initialize(keylength);
    }

    public void createKeys() {
        this.pair = this.keyGen.generateKeyPair();
        this.privateKey = pair.getPrivate();
        this.publicKey = pair.getPublic();
    }

    public PrivateKey getPrivateKey() {
        return this.privateKey;
    }

    public PublicKey getPublicKey() {
        return this.publicKey;
    }

    /**
     * Converts public Key to String
     * @param publicKey the Public Key
     * @return the public key as String
     */
    public static String publicKeyToString(PublicKey publicKey){
        //converting public key to byte
        byte[] byte_pubkey = publicKey.getEncoded();

        //converting byte to String
        String str_key = Base64.getEncoder().encodeToString(byte_pubkey);
        return str_key;
    }

    /**
     * Converts String to public KEy
     * @param str_key the String
     * @return the public Key
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     */
    public static PublicKey stringToPublicKey(String str_key) throws InvalidKeySpecException, NoSuchAlgorithmException
    {
        //converting string to Bytes
        byte[] byte_pubkey  = Base64.getDecoder().decode(str_key);


        //converting it back to public key
        KeyFactory factory = KeyFactory.getInstance("RSA");
        PublicKey publicKeyBack = (PublicKey) factory.generatePublic(new X509EncodedKeySpec(byte_pubkey));
        return publicKeyBack;
    }

    /**
     * Converts private Key to String
     * @param privateKey the Private Key
     * @return the private key as String
     */
    public static String privateKeyToString(PrivateKey privateKey){
        //converting public key to byte
        byte[] byte_pubkey = privateKey.getEncoded();

        //converting byte to String
        String str_key = Base64.getEncoder().encodeToString(byte_pubkey);
        return str_key;
    }

    /**
     * Converts String to private KEy
     * @param str_key the String
     * @return the private Key
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     */
    public static PrivateKey stringToPrivateKey(String str_key) throws InvalidKeySpecException, NoSuchAlgorithmException
    {
        //converting string to Bytes
        byte[] byte_pubkey  = Base64.getDecoder().decode(str_key);


        //converting it back to public key
        KeyFactory factory = KeyFactory.getInstance("RSA");
        PrivateKey privateKeyBack = (PrivateKey) factory.generatePrivate(new PKCS8EncodedKeySpec(byte_pubkey));
        return privateKeyBack;
    }

    /**
     * encrypts a message
     * @param msg the message
     * @param publicKey the publicKey
     * @return
     */
    public static String encryptMessage(String secretMessage, PublicKey publicKey){

        String encryptedMessage = new String();
        try
        {
            Cipher encryptCipher = Cipher.getInstance("RSA");
            encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] secretMessageBytes = secretMessage.getBytes(StandardCharsets.UTF_8);
            byte[] encryptedMessageBytes = encryptCipher.doFinal(secretMessageBytes);
            Utility.safePrintln(String.valueOf(encryptedMessageBytes.length));
            String encodedString = Base64.getEncoder().encodeToString(encryptedMessageBytes);
            encryptedMessage = encodedString;
        }
        catch (InvalidKeyException e)
        {
            e.printStackTrace(); Utility.safeDebugPrintln("error: " +e.getMessage());
        }
        catch (NoSuchPaddingException e)
        {
            e.printStackTrace(); Utility.safeDebugPrintln("error: " +e.getMessage());
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace(); Utility.safeDebugPrintln("error: " +e.getMessage());
        }
        catch (IllegalBlockSizeException e)
        {
            e.printStackTrace(); Utility.safeDebugPrintln("error: " +e.getMessage());
        }
        catch (BadPaddingException e)
        {
            e.printStackTrace(); Utility.safeDebugPrintln("error: " +e.getMessage());
        }


        return encryptedMessage;
    }

    /**
     * Decrypts A message
     * @param msg message as byte Array
     * @param privateKey the privat key
     * @return the decryptedMessage
     */
    public static String decryptMessage(String encryptedMessage, PrivateKey privateKey){
        String decryptedMessage = new String();
        try
        {
            Cipher decryptCipher = Cipher.getInstance("RSA");
            decryptCipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] encryptedMessageBytes = Base64.getDecoder().decode(encryptedMessage);;
            Utility.safePrintln(String.valueOf(encryptedMessageBytes.length));
            byte[] decryptedMessageBytes = decryptCipher.doFinal(encryptedMessageBytes);
            decryptedMessage = new String(decryptedMessageBytes, StandardCharsets.UTF_8);
        }
        catch (InvalidKeyException e)
        {
            e.printStackTrace(); Utility.safeDebugPrintln("error: " +e.getMessage());
        }
        catch (NoSuchPaddingException e)
        {
            e.printStackTrace(); Utility.safeDebugPrintln("error: " +e.getMessage());
        }
        catch (IllegalBlockSizeException e)
        {
            e.printStackTrace(); Utility.safeDebugPrintln("error: " +e.getMessage());
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace(); Utility.safeDebugPrintln("error: " +e.getMessage());
        }
        catch (BadPaddingException e)
        {
            e.printStackTrace(); Utility.safeDebugPrintln("error: " +e.getMessage());
        }

        return  decryptedMessage;
    }



    public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException
    {
        GenerateKeys gk;
        gk = new GenerateKeys(1024);
        gk.createKeys();

        PublicKey publicKey = gk.getPublicKey();
        String publicKeyAsString = publicKeyToString(publicKey);
        PublicKey publicKeyBack = stringToPublicKey(publicKeyAsString);


        String message = "HEllo World!";

        String encrypted = encryptMessage(message,gk.getPublicKey());
        String decrypted = decryptMessage(encrypted,gk.getPrivateKey());


        Utility.safePrintln(decrypted);





        if (publicKey.equals(publicKeyBack)){
            Utility.safePrintln("succesfull");
        }
        /*
        try {
            gk = new GenerateKeys(1024);
            gk.createKeys();
            gk.writeToFile("KeyPair/publicKey", gk.getPublicKey().getEncoded());
            gk.writeToFile("KeyPair/privateKey", gk.getPrivateKey().getEncoded());
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            System.err.println(e.getMessage());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }*/
    }


}