package application;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main extends Application {

    private SSOController ssoController = new SSOController();
    private Label messageLabel;
    private int loginAttempts = 0;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Single Sign-On with JavaFX");
// dif pics : https://4.bp.blogspot.com/_LhXNcvetC7I/S05eIzUsgPI/AAAAAAAAAFw/4RCuvxgmBhQ/w1200-h630-p-k-no-nu/ChowdersDead.jpg
        // Load image from a URL
        Image image = new Image("https://4.bp.blogspot.com/_LhXNcvetC7I/S05eIzUsgPI/AAAAAAAAAFw/4RCuvxgmBhQ/w1200-h630-p-k-no-nu/ChowdersDead.jpg"); // default pic add profiles later
        ImageView imageView = new ImageView(image);

        VBox vbox = new VBox(10); // 10 is the spacing between nodes
        vbox.setAlignment(Pos.CENTER);

        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter username");
        usernameField.setMinWidth(150); // Set the minimum width for the username field
        usernameField.setMaxWidth(300);
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");
        passwordField.setMinWidth(150); // Set the minimum width for the password field
        passwordField.setMaxWidth(300);
        Button loginButton = new Button("Login");
        messageLabel = new Label();

        // Set event handler for the login button
        loginButton.setOnAction(event -> handleLogin(usernameField.getText(), passwordField.getText()));

        vbox.getChildren().addAll(imageView, usernameField, passwordField, loginButton, messageLabel);

        primaryStage.setScene(new Scene(vbox, 400, 300));
        primaryStage.show();
    }

    private void handleLogin(String username, String password) {
        String encryptedToken = ssoController.login(username, password);

        if (encryptedToken != null) {
            messageLabel.setText("Login successful! Encrypted Token: " + encryptedToken);
        } else {
            loginAttempts++;

            if (loginAttempts < 3) {
                messageLabel.setText("Invalid credentials. Please try again. Attempts left: " + (3 - loginAttempts));
            } else {
                messageLabel.setText("Maximum login attempts reached. Closing in 10 seconds.");

                // Schedule a task to close the application after 10 seconds
                ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
                scheduler.schedule(() -> Platform.exit(), 10, TimeUnit.SECONDS);
                scheduler.shutdown();
            }
        }
    }

    /**
     * Controller for Single Sign-On operations.
     */
    public static class SSOController {

        // Generate a random Triple DES key
        private final String secretKey = generateRandomKey();

        // Constants for demonstration purposes
        private final String username = "user123";
        private final String password = "password123";

        /**
         * Simulates user login and generates an encrypted token.
         *
         * @param user The provided username.
         * @param pass The provided password.
         * @return Encrypted token if login is successful, otherwise null.
         */
        public String login(String user, String pass) {
            if (user.equals(username) && pass.equals(password)) {
                // Generate a token for the authenticated user
                String token = generateToken(user);
                // Encrypt the token using Triple DES
                return encrypt(token, secretKey);
            } else {
                return null; // Invalid credentials
            }
        }

        /**
         * Generates a simple token based on the username and current timestamp.
         *
         * @param username The username for which the token is generated.
         * @return The generated token.
         */
        private String generateToken(String username) {
            return "User:" + username + ":Timestamp:" + System.currentTimeMillis();
        }

        /**
         * Triple DES encryption of a given plain text using a secret key.
         *
         * @param plainText The text to be encrypted.
         * @param key The secret key used for encryption.
         * @return The Base64-encoded encrypted text.
         */
        private String encrypt(String plainText, String key) {
            try {
                DESedeKeySpec keySpec = new DESedeKeySpec(Base64.getDecoder().decode(key));
                SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DESede");
                SecretKey secretKey = keyFactory.generateSecret(keySpec);

                Cipher cipher = Cipher.getInstance("DESede");
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);

                // Encrypt the plain text
                byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
                // Encode the encrypted bytes to Base64 for better representation
                return Base64.getEncoder().encodeToString(encryptedBytes);
            } catch (Exception e) {
                // Print an error message if encryption fails
                System.err.println("Error during encryption: " + e.getMessage());
                return null;
            }
        }

        /**
         * Generates a random Triple DES key.
         *
         * @return The Base64-encoded random key.
         */
        /**
         * Generates a random Triple DES key.
         *
         * fixed so returns right amount of values per key
         *
         * @return The Base64-encoded random key.
         */
        private String generateRandomKey() {
            try {
                SecureRandom secureRandom = new SecureRandom();
                byte[] keyBytes = new byte[24]; // Use 24 bytes for 192-bit key
                secureRandom.nextBytes(keyBytes);
                // Set parity bits for each 8-byte block
                for (int i = 0; i < 24; i += 8) {
                    keyBytes[i + 7] = keyBytes[i + 6] = (byte) ((keyBytes[i] & 0xFE) >>> 1);
                }
                return Base64.getEncoder().encodeToString(keyBytes);
            } catch (Exception e) {
                System.err.println("Error generating random key: " + e.getMessage());
                return null;
            }
        }
    }
}
