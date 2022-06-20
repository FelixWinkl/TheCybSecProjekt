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
    protected Communicator _communicator;

    /**
     * Creates a new task.
     *
     * @param socketInputStream  The socket input stream.
     * @param socketOutputStream The socket output stream.
     */
    public Task(DataInputStream socketInputStream, DataOutputStream socketOutputStream, ClientConfiguration clientConfiguration)
    {
        // Save parameters
        _socketInputStream = socketInputStream;
        _socketOutputStream = socketOutputStream;
        _clientConfiguration = clientConfiguration;
        _communicator = new Communicator(clientConfiguration.get_serverPublicKey());
    }

    /**
     * Executes the task.
     *
     * @throws IOException
     */
    public abstract void run() throws IOException;
}
