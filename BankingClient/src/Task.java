import javax.crypto.SecretKey;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Defines an action a user can do (like a server login).
 */
public abstract class Task
{
    /**
     * The socket input stream.
     */
    protected DataInputStream _socketInputStream;

    /**
     * The socket output stream.
     */
    protected DataOutputStream _socketOutputStream;

    /**
     * configuration of the client
     */
    protected ClientConfiguration _clientConfiguration;

    /**
     * Obejct which handles all communictaion with the server.
     */
    protected Config _config;

    /**
     * Creates a new task.
     *
     * @param socketInputStream  The socket input stream.
     * @param socketOutputStream The socket output stream.
     */
    public Task(DataInputStream socketInputStream, DataOutputStream socketOutputStream, ClientConfiguration clientConfiguration, SecretKey symmetricKey)
    {
        // Save parameters
        _socketInputStream = socketInputStream;
        _socketOutputStream = socketOutputStream;
        _clientConfiguration = clientConfiguration;
        _config = new Config(clientConfiguration.get_serverPublicKey(),symmetricKey);
    }

    /**
     * Executes the task.
     *
     * @throws IOException
     */
    public abstract void run() throws IOException;
}
