import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

/**
 * Class for AES en- and decryption
 */
public class AESHelper
{

    /**
     * generate AES key
     * @param n size
     * @return the key
     * @throws NoSuchAlgorithmException
     */
    public static SecretKey generateKey(int n) throws NoSuchAlgorithmException
    {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(n);
        SecretKey key = keyGenerator.generateKey();
        return key;
    }

    /**
     * Generate Iv
     * @return the IV
     */
    public static IvParameterSpec generateIv() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    /**
     * encrypt a massage
     * @param input the message to encrypt
     * @param key the AES key
     * @param iv the IV
     * @return the encrypted Message
     */
    public static String encrypt( String input, SecretKey key,
                                 IvParameterSpec iv)
    {

        try
        {
            String algorithm = "AES/CBC/PKCS5Padding";
            Cipher cipher = null;
            cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            byte[] cipherText = cipher.doFinal(input.getBytes());
            return Base64.getEncoder()
                    .encodeToString(cipherText);
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        catch (NoSuchPaddingException e)
        {
            e.printStackTrace();
        }
        catch (InvalidAlgorithmParameterException e)
        {
            e.printStackTrace();
        }
        catch (IllegalBlockSizeException e)
        {
            e.printStackTrace();
        }
        catch (BadPaddingException e)
        {
            e.printStackTrace();
        }
        catch (InvalidKeyException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * decrypt a massage
     * @param cipherText the message to decrypt
     * @param key the AES key
     * @param iv the IV
     * @return the decrypted Message
     */
    public static String decrypt( String cipherText, SecretKey key,
                                 IvParameterSpec iv)  {
        String algorithm = "AES/CBC/PKCS5Padding";
        try
        {
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            byte[] plainText = new byte[0];
            plainText = cipher.doFinal(Base64.getDecoder()
                    .decode(cipherText));
            return new String(plainText);
        }
        catch (IllegalBlockSizeException e)
        {
            e.printStackTrace();
        }
        catch (BadPaddingException e)
        {
            e.printStackTrace();
        }
        catch (InvalidAlgorithmParameterException e)
        {
            e.printStackTrace();
        }
        catch (NoSuchPaddingException e)
        {
            e.printStackTrace();
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        catch (InvalidKeyException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static String secretKeyToString(SecretKey key){
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public static SecretKey stringToSecreteKey(String keyString) throws InvalidKeySpecException, NoSuchAlgorithmException
    {
        byte[] byte_key = Base64.getDecoder().decode(keyString);


        //converting it back to public key
        SecretKey secretKey=  new SecretKeySpec(byte_key, 0, byte_key.length, "AES");
        return secretKey;
    }

    /**
     * Just for testing
     */
    public static void main(String args[]) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidKeySpecException
    {
        String msg = "hello Wordsdssdsd asDadsdasdsadssds";
        SecretKey symmetricKey = AESHelper.generateKey(128);
        IvParameterSpec iv = generateIv();


        String encrypted = AESHelper.encrypt(msg,symmetricKey,iv);

        String ivString = ivToString(iv);
        String secretKeyString = secretKeyToString(symmetricKey);
        SecretKey afterTransKey = stringToSecreteKey(secretKeyString);
        IvParameterSpec ivAfterString = stringToIv(ivString);
                String decrypted = AESHelper.decrypt(encrypted,afterTransKey,ivAfterString);
                System.out.println(ivString.length());
        System.out.println(decrypted);
    }

    public static String ivToString(IvParameterSpec iv){
        return Base64.getEncoder().encodeToString(iv.getIV());
    }

    public static IvParameterSpec stringToIv(String ivString){
        return new IvParameterSpec(Base64.getDecoder().decode(ivString));
    }


}
