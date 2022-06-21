import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

/**
 * Contains client configuration.
 */
public class ClientConfiguration
{
    /**
     * The implementation version.
     */
    private String _version;


    /**
     * The public Key of the Server
     */
    private PublicKey _serverPublicKey;

    /**
     * Reads the configuration data from the given JSON file.
     */
    public ClientConfiguration(String configurationFilePath)
    {
        try (InputStream jsonFileStream = new FileInputStream(configurationFilePath))
        {
            // Retrieve root object
            JsonReader jsonReader = Json.createReader(jsonFileStream);
            JsonObject rootObj = jsonReader.readObject();

            // Read data
            _version = rootObj.getString("version");

            //Read Server publicKey
            String publicKeyString = rootObj.getString("pK");
            try
            {
                _serverPublicKey = GenerateKeys.stringToPublicKey(publicKeyString);
            }
            catch (InvalidKeySpecException e)
            {
                Utility.safeDebugPrintln("error l49: " +e.getMessage());
            }
            catch (NoSuchAlgorithmException e)
            {
                Utility.safeDebugPrintln("error l53: " +e.getMessage());
            }


            // Release reader resources
            jsonReader.close();
        }
        catch (IOException e)
        {
            Utility.safeDebugPrintln("error l62: " +e.getMessage());
        }
    }

    /**
     * Returns the implementation version.
     *
     * @return The implementation version.
     */
    public String getVersion()
    {
        return _version;
    }

    /** Returns the public Key of the Server
     *
     * @return the public Key of the Server
     */
    public PublicKey get_serverPublicKey()
    {
        return _serverPublicKey;
    }

}
