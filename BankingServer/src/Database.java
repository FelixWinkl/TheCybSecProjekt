import javax.json.*;
import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

/**
 * Contains and manages user data. This class is thread safe.
 */
public class Database
{
    /**
     * The JSON file the database is stored in.
     */
    private String _databaseFile;

    /**
     * Contains the user data like name, password in device list.
     */
    private LinkedList<UserData> _users;

    /**
     * Loads the given database JSON file.
     */
    public Database(String databaseFile)
    {
        // Open JSON file
        _databaseFile = databaseFile;
        try (InputStream jsonFileStream = new FileInputStream(databaseFile))
        {
            // Retrieve root object
            JsonReader jsonReader = Json.createReader(jsonFileStream);
            JsonObject rootObj = jsonReader.readObject();

            // Read user data
            _users = new LinkedList<>();
            JsonArray usersArr = rootObj.getJsonArray("users");
            for (JsonObject userDataObj : usersArr.getValuesAs(JsonObject.class))
                _users.add(new UserData(userDataObj));

            // Release reader resources
            jsonReader.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Creates a new, empty database file. Used by the generate() function.
     */
    private Database()
    {
        // Initialize empty user list
        _users = new LinkedList<>();
    }

    /**
     * Generates a new database file (utility function to generate lab
     * configuration).
     *
     * @param databaseFile            The new database's file name.
     * @param mitmPasswordFile        The file to store the Man-in-the-Middle
     *                                password.
     * @param clientConfigurationFile The file to store the default client
     *                                configuration.
     * @param credentialsFile         The file to store the credentials known to the
     *                                attacker.
     */
    public static void generate(String databaseFile, String mitmPasswordFile, String clientConfigurationFile, String credentialsFile)
    {
        // Start database object
        Database database = new Database();
        database._databaseFile = databaseFile;

        // Password generator (n decimal digits)
        Random rand = new Random();
        Function<Integer, String> passwordGen = (Integer length) ->
        {
            String randomDigits = "";
            for (int i = 0; i < length; ++i)
                randomDigits += Math.abs(rand.nextInt() % 10);
            return randomDigits;
        };

        // Create attacker users
        database._users.add(new UserData("group", "0000", 1000, -1));

        // Create victim 1
        String password1 = passwordGen.apply(10);
        database._users.add(new UserData("victim1", password1, 1000, 1));

        // Create victim 2
        String password2 = passwordGen.apply(6);
        database._users.add(new UserData("victim2", password2, 1000, 2));

        // Create victim 3
        String password3 = passwordGen.apply(6);
        database._users.add(new UserData("victim3", password3, 1000, 3));

        // Create dummy user
        database._users.add(new UserData("dummy", "0000", 1000, -1));

        // Save database
        database.Save();

        // Create MITM file
        try (FileWriter mitmFileWriter = new FileWriter(mitmPasswordFile))
        {
            // Write needed information
            mitmFileWriter.write(password1);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        // Create client configuration file
        try (FileWriter clientConfigurationFileWriter = new FileWriter(clientConfigurationFile))
        {
            // Write default configuration
            clientConfigurationFileWriter.write("{\n");
            clientConfigurationFileWriter.write("    \"version\": \"ITS Banking System v1.2c\"\n");
            clientConfigurationFileWriter.write("}\n");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        // Create credential file for attacker
        try (FileWriter credentialsFileWriter = new FileWriter(credentialsFile))
        {
            // Write needed information
            credentialsFileWriter.write("Login Attacker: group\n");
            credentialsFileWriter.write("      Password: 0000\n");
            credentialsFileWriter.write("\n");
            credentialsFileWriter.write("Login Scenario 1: victim1\n");
            credentialsFileWriter.write("\n");
            credentialsFileWriter.write("Login Scenario 2: victim2\n");
            credentialsFileWriter.write("        Password: " + password2 + "\n");
            credentialsFileWriter.write("\n");
            credentialsFileWriter.write("Login Scenario 3: victim3\n");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Saves the database. Use only for generating databases, and testing.
     */
    private void Save()
    {
        Utility.safePrintln("Saving database...");

        // Retrieve user data
        JsonArrayBuilder usersArrayBuilder = Json.createArrayBuilder();
        for (UserData userData : _users)
            usersArrayBuilder.add(userData.toJson());

        // Build root object
        JsonObjectBuilder rootObjBuilder = Json.createObjectBuilder();
        rootObjBuilder.add("users", usersArrayBuilder.build());

        // Create output JSON file
        try (OutputStream jsonFileStream = new FileOutputStream(_databaseFile))
        {
            // Write JSON data
            JsonWriter jsonWriter = Json.createWriter(jsonFileStream);
            jsonWriter.writeObject(rootObjBuilder.build());

            // Release writer resources
            jsonWriter.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Returns the name of the given user.
     *
     * @param userId User ID.
     * @return The name of the given user.
     */
    public String getUserName(int userId)
    {
        // Return name
        if (userId >= 0 && userId < _users.size())
            return _users.get(userId).getName();
        return null;
    }

    /**
     * Checks whether the given credentials belong to a user, and returns his/her
     * ID.
     *
     * @param name     The name of the user.
     * @param password The password of the user.
     * @return The ID of the user if the login is right, else -1.
     */
    public int verifyLogin(String name, String password)
    {
        // Run through users and search for given credentials
        for (int u = 0; u < _users.size(); ++u)
        {
            // Correct name?
            UserData currU = _users.get(u);
            if (currU.getName().equalsIgnoreCase(name))
            {
                // Check password
                if (currU.checkPassword(password))
                    return u;
            }
        }
        return -1;
    }

    /**
     * Adds the device with the given authentication code to the given user.
     *
     * @param userId     The ID of the user where the new device shall be added.
     * @param deviceCode The device code to be added.
     */
    public void addUserDevice(int userId, String deviceCode)
    {
        // Add device
        if (userId >= 0 && userId < _users.size())
            _users.get(userId).addDevice(deviceCode);
    }

    /**
     * Checks whether the given user has a device with the given ID.
     *
     * @param userId     The ID of the user to be checked.
     * @param deviceCode The device code to be checked.
     * @return Whether the given user has a device with the given ID.
     */
    public boolean userHasDevice(int userId, String deviceCode)
    {
        // Check device
        if (userId >= 0 && userId < _users.size())
            return _users.get(userId).hasDevice(deviceCode);
        return false;
    }

    /**
     * Retrieves the amount of money of the given user.
     *
     * @param userId The ID of the user whose amount of money shall be retrieved.
     * @return The amount of money of the given user.
     */
    public int getMoney(int userId)
    {
        // Return money
        if (userId >= 0 && userId < _users.size())
            return _users.get(userId).getMoney();
        return -1;
    }

    /**
     * Sends money from the given source user to the given target user.
     *
     * @param sourceUserId   The ID of the user where the money comes from.
     * @param targetUserName The name of the user where the money is sent to.
     * @param amount         The (positive) amount of money being sent to the target
     *                       user.
     * @return A boolean indicating whether sending money was successful.
     */
    public boolean sendMoney(int sourceUserId, String targetUserName, int amount)
    {
        // Test whether users exist
        if (sourceUserId < 0 || sourceUserId >= _users.size())
            return false;
        int targetUserId = -1;
        for (int u = 0; u < _users.size(); ++u)
            if (_users.get(u).getName().equalsIgnoreCase(targetUserName))
            {
                targetUserId = u;
                break;
            }
        if (targetUserId < 0)
            return false;

        // Test whether source user has enough money
        if (amount <= 0 || _users.get(sourceUserId).getMoney() < amount)
            return false;

        // Send money
        _users.get(sourceUserId).changeMoney(targetUserId, -amount);
        _users.get(targetUserId).changeMoney(sourceUserId, amount);

        // Scenario victim account?
        int scenarioId = _users.get(sourceUserId).getScenarioId();
        if (targetUserName.equalsIgnoreCase("group") && scenarioId > 0)
            LabEnvironment.notifyScenarioSolved(scenarioId);
        return true;
    }

    /**
     * Returns a string containing the given user's full money sending/receiving
     * history.
     *
     * @param userId The ID of the user whose history is requested.
     * @return A map containing the given user's full money sending/receiving
     * history.
     */
    public Map<String, Integer> getUserMoneyHistory(int userId)
    {
        // Check parameters history
        if (userId < 0 || userId >= _users.size())
            return null;

        // Get user's history
        LinkedList<Tuple<Integer, Integer>> history = _users.get(userId).getMoneyHistory();

        // Build history mapping user names to amounts
        Map<String, Integer> historyMap = new HashMap<>();
        for (Tuple<Integer, Integer> entry : history)
            historyMap.put(_users.get(entry.x).getName(), entry.y);
        return historyMap;
    }
}
