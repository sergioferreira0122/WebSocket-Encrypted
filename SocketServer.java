import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketServer {

    // location in the command line arguments' array where the path is provided
    static int COMMAND_LINE_ARGUMENT_FILE_PATH = 0;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter output;
    private BufferedReader input;
    private final EncryptionAlgorithm encryptionAlgorithm;

    public SocketServer(EncryptionAlgorithm encryptionAlgorithm) {
        this.encryptionAlgorithm = encryptionAlgorithm;
    }

    /**
     * __Reminders:__
     * - validate inputs
     * - Start the server service & configure the encryption algorithm
     * - handle errors
     * - terminate the service on demand
     *
     * @param args - command line arguments. args[0] SHOULD contain the absolute path for the configuration file
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: java SocketServer <config_file_path>");
        }

        EncryptionAlgorithm encryptionAlgorithm = new CaesarEnigma(args[COMMAND_LINE_ARGUMENT_FILE_PATH]);

        SocketServer server = new SocketServer(encryptionAlgorithm);
        server.start(5001);
        server.processServerCommunications();
        server.stop();

        System.out.println("Closing socket...");
    }

    /**
     * start the server and wait for connections on the given port
     *
     * @param port - port to connect the client to
     */
    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            clientSocket = serverSocket.accept();
            output = new PrintWriter(clientSocket.getOutputStream());
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            System.out.println("Opening port " + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handle messages received on the server and log them with the respective timestamp
     */
    public void processServerCommunications() {
        try {
            output = new PrintWriter(clientSocket.getOutputStream());
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String inputLine;
            while ((inputLine = input.readLine()) != null) {
                long currentTimeMillis = System.currentTimeMillis();
                String inputLineDecrypted = this.decrypt(inputLine);
                System.out.println("[" + currentTimeMillis +"] " + "message received: " + "(" + inputLine + ") " + inputLineDecrypted);
                output.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Terminate the service - close ALL I/O
     */
    public void stop() {
        try {
            serverSocket.close();
            clientSocket.close();
            output.close();
            input.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Decrypts the given message using the instance's encryption algorithm
     *
     * @param message - message to be decrypted
     * @return the decrypted (original) message
     */
    private String decrypt(String message) {
        return this.encryptionAlgorithm.decrypt(message);
    }

    /**
     * Mostly useful for debug purposes
     */
    @Override
    public String toString() {
        return "Server Configuration{" +
                "server Address=" + serverSocket.getInetAddress() +
                ", clientSocket=" + serverSocket.getLocalPort() +
                '}' +
                "EncryptionAlgorithm: " + encryptionAlgorithm;
    }
}