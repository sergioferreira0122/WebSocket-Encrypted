/**
 * The interface that EnhancedCaesar should implement to be able to interact with the SocketServer & SocketClient
 */
public interface EncryptionAlgorithm {

    String encrypt(String cleartext);

    String decrypt(String ciphertext);

    /**
     * Configure (using the input file) and returns a new instance of the encryption algorithm
     *
     * @param configurationFilePath - the absolute path where the configuration file is stored
     */
    void configure(String configurationFilePath) throws Exception;
}