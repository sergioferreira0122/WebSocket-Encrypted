import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class CaesarEnigma implements EncryptionAlgorithm {
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "1234567890";
    private static final String PUNCTUATION = "\\|!#$%&*+-_:;<=>^?@";
    private int rot;
    private int f;
    private HashMap<Character, Character> plugboard = new HashMap<>();
    private String alphabet;
    private int alphabet_size;

    public CaesarEnigma(String filePath) {
        try {
            configure(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String encrypt(String cleartext) {
        cleartext = ApplyPlugBoard(cleartext);

        StringBuilder encryptedWord = new StringBuilder();

        for (int i = 0; i < cleartext.length(); i++) {
            char currentChar = cleartext.charAt(i);

            int inc = i * this.f;
            char shiftedChar;

            int alphabetIndex = alphabet.indexOf(currentChar);
            if (alphabetIndex != -1) {
                int shiftedCharIndex = (alphabetIndex + this.rot + inc) % alphabet_size;
                shiftedChar = alphabet.charAt(shiftedCharIndex);
            } else {
                shiftedChar = currentChar;
            }

            encryptedWord.append(shiftedChar);
        }

        return encryptedWord.toString();
    }

    @Override
    public String decrypt(String ciphertext) {
        StringBuilder decryptedWord = new StringBuilder();

        for (int i = 0; i < ciphertext.length(); i++) {
            char currentChar = ciphertext.charAt(i);

            int inc = i * this.f;
            char shiftedChar;

            int alphabetIndex = alphabet.indexOf(currentChar);
            if (alphabetIndex != -1) {
                int shiftedCharIndex = (alphabetIndex - this.rot - inc) % alphabet_size;
                if (shiftedCharIndex < 0) {
                    shiftedCharIndex += alphabet_size;
                }
                shiftedChar = alphabet.charAt(shiftedCharIndex);
            } else {
                shiftedChar = currentChar;
            }

            decryptedWord.append(shiftedChar);
        }

        String word = decryptedWord.toString();

        return ApplyPlugBoard(word) ;
    }

    private String ApplyPlugBoard(String word) {
        char[] modifiedWord = word.toCharArray();

        for (int i = 0; i < modifiedWord.length; i++) {
            char currentChar = modifiedWord[i];

            if (UPPER.contains(String.valueOf(currentChar)) && plugboard.containsKey(currentChar)) {
                modifiedWord[i] = plugboard.get(currentChar);
            }
        }

        return new String(modifiedWord);
    }

    @Override
    public void configure(String configurationFilePath) throws IOException {
        try {
            File xmlFile = new File(configurationFilePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);

            doc.getDocumentElement().normalize();

            NodeList alphabetNodeList = doc.getElementsByTagName("alphabet");
            Element alphabetElement = (Element) alphabetNodeList.item(0);
            String alphabetOptions = alphabetElement.getTextContent();
            alphabet = GetAlphabetFromOptions(alphabetOptions);

            rot = ParseIntFromElement(doc, "encryption-key");

            f = ParseIntFromElement(doc, "increment-factor");

            plugboard = ParsePlugboard(doc);

            alphabet_size = alphabet.length();
        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException("Error parsing XML configuration file", e);
        }
    }

    private static String GetAlphabetFromOptions(String alphabetOptions) {
        String[] options = alphabetOptions.split("\\+");
        StringBuilder result = new StringBuilder();

        for (String option : options) {
            switch (option.trim()) {
                case "UPPER" -> result.append(UPPER);
                case "DIGITS" -> result.append(DIGITS);
                case "PUNCTUATION" -> result.append(PUNCTUATION);
                default -> throw new IllegalArgumentException("Invalid alphabet option: " + option);
            }
        }

        return result.toString();
    }

    private static int ParseIntFromElement(Document doc, String tagName) {
        Element element;
        int value;
        try {
            NodeList nodeList = doc.getElementsByTagName(tagName);
            element = (Element) nodeList.item(0);
            value = Integer.parseInt(element.getTextContent().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Not a integer in encryption-key or increment-factor");
        }
        return value;
    }

    private static HashMap<Character, Character> ParsePlugboard(Document doc) {
        NodeList plugboardNodeList = doc.getElementsByTagName("plugboard");
        Element plugboardElement = (Element) plugboardNodeList.item(0);
        String plugboardText = plugboardElement.getTextContent().trim().replaceAll("[{}']", "");
        String[] pairs = plugboardText.split(", ");
        HashMap<Character, Character> result = new HashMap<>();

        for (String pair : pairs) {
            String[] keyValue = pair.split(":");

            char keyChar = keyValue[0].trim().charAt(0);
            char valueChar = keyValue[1].trim().charAt(0);

            if (result.containsValue(valueChar) || result.containsKey(keyChar)) {
                throw new IllegalArgumentException("Plugboard configuration contains collisions.");
            }

            result.put(keyChar, valueChar);
        }

        return result;
    }


    @Override
    public String toString() {
        return "CaesarEnigma{" +
                "rot=" + rot +
                ", f=" + f +
                ", plugboard=" + plugboard +
                ", alphabet='" + alphabet + '\'' +
                ", alphabet_size=" + alphabet_size +
                '}';
    }
}
