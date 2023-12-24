import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class SocketClient {
    // location in the command line arguments' array where the path is provided
    static int COMMAND_LINE_ARGUMENT_FILE_PATH = 0;
    // server IP address
    final String SERVER_IP = "127.0.0.1";
    final String CLOSE_COMMAND = "BYE";
    private Socket socket;
    private Scanner inputScanner;
    private PrintWriter output;
    private BufferedReader input;
    private final EncryptionAlgorithm encryptionAlgorithm;

    public SocketClient(EncryptionAlgorithm encryptionAlgorithm) {
        this.encryptionAlgorithm = encryptionAlgorithm;
    }

    /**
     * __Reminders:__
     * - validate inputs
     * - connect to the server & configure the encryption algorithm
     * - handle errors
     *
     * @param args - command line arguments. args[0] SHOULD contain the absolute path for the configuration file
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: java SocketClient <config_file_path>");
        }

        EncryptionAlgorithm encryptionAlgorithm = new CaesarEnigma(args[COMMAND_LINE_ARGUMENT_FILE_PATH]);

        SocketClient socketClient = new SocketClient(encryptionAlgorithm);
        socketClient.startConnection(5001);

        socketClient.processUserInput();
    }

    /**
     * connect the client to the server at `SERVER_IP` on the given port
     *
     * @param port - port to connect the client to
     */
    public void startConnection(int port) {
        try {
            socket = new Socket(SERVER_IP, port);
            inputScanner = new Scanner(System.in);
            output = new PrintWriter(socket.getOutputStream(), true);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.println("SocketClient started. Start typing your messages...");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void processUserInput() {
        String clientInput;
        while (!socket.isClosed()) {
            clientInput = inputScanner.nextLine().toUpperCase();
            handleClientCommunications(clientInput);
        }
    }

    /**
     * Handle the client's communications
     */
    public void handleClientCommunications(String clientInput) {
        if (clientInput.equalsIgnoreCase(CLOSE_COMMAND)) {
            sendCloseCommand();
        }

        String encryptedInput = encrypt(clientInput);
        output.println(encryptedInput);
    }

    /**
     * notify the server to close the session
     */
    private void sendCloseCommand() {
        try {
            stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Close ALL client I/O
     */
    public void stop() throws IOException {
        inputScanner.close();
        output.close();
        input.close();
        socket.close();
    }

    /**
     * Encrypts the given message using the instance's encryption algorithm
     *
     * @param message - Message to be encrypted
     * @return the encrypted message
     */
    private String encrypt(String message) {
        return this.encryptionAlgorithm.encrypt(message);
    }

    /**
     * Mostly useful for debug purposes
     */
    public String toString() {
        return "Client Configuration{" +
                "socket Address=" + socket.getInetAddress() +
                ", clientSocket=" + socket.getPort() +
                '}' +
                "EncryptionAlgorithm: " + encryptionAlgorithm;
    }
}
