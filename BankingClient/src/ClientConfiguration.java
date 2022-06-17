import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

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

            // Release reader resources
            jsonReader.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
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
}
