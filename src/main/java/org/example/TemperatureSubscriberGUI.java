package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import javax.net.ssl.SSLSocketFactory;

import static java.nio.charset.StandardCharsets.UTF_8;

public class TemperatureSubscriberGUI extends Application {
    private MqttClient client;
    private Label temperatureLabel;
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Temperature Subscriber");
        temperatureLabel = new Label("Temperature (Celsius): ");
        temperatureLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        temperatureLabel.setAlignment(Pos.CENTER);
        VBox vBox = new VBox(10);
        vBox.getChildren().add(temperatureLabel);
        Image backgroundImage = new Image("https://france3-regions.francetvinfo.fr/image/G-yqipUeFCoFvuIOVHFZ_YHCUgE/600x400/regions/2023/07/25/64bfd81855e06_design-sans-titre-9.jpg"); // Replace with your actual image path
        BackgroundImage background = new BackgroundImage(backgroundImage,
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
        vBox.setBackground(new Background(background));
        vBox.setPadding(new Insets(20));
        vBox.setAlignment(Pos.CENTER);
        Scene scene = new Scene(vBox, 500, 300);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> {
            try {
                disconnect();
            } catch (MqttException mqttException) {
                mqttException.printStackTrace();
            }
            Platform.exit();
            System.exit(0);
        });

        primaryStage.show();
        try {
            initializeMqttClient();
            subscribeToTemperatureTopic();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    private void initializeMqttClient() throws MqttException {
        client = new MqttClient(
                "ssl://9b48d61e902a4003b611c45f4a57e510.s2.eu.hivemq.cloud:8883",
                MqttClient.generateClientId(),
                new MemoryPersistence());

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setUserName("Bouthaina");
        mqttConnectOptions.setPassword("_8UecSQBHqtAqfX".toCharArray());
        mqttConnectOptions.setSocketFactory(SSLSocketFactory.getDefault());

        client.connect(mqttConnectOptions);

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                System.out.println("Connection lost. Reconnecting...");
                try {
                    connect(client, mqttConnectOptions);
                    subscribeToTemperatureTopic();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void messageArrived(String topic, MqttMessage message) {
                updateTemperatureLabel(new String(message.getPayload(), UTF_8));
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                // Called when an outgoing publish is complete
            }
        });
    }
    private void subscribeToTemperatureTopic() {
        try {
            client.subscribe("Temp", 1);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    private void updateTemperatureLabel(String temperature) {
        Platform.runLater(() -> {
            temperatureLabel.setText("Temperature(Celsius): " + temperature);
            checkTemperature(parseTemperature(temperature));
        });
    }
    private double parseTemperature(String temperature) {
        // Replace comma with period and parse the temperature
        temperature = temperature.replace(",", ".");
        return Double.parseDouble(temperature);
    }

    private void checkTemperature(double temperature) {
        if (temperature > 30) {
            showAlert("High Temperature Alert", "Temperature exceeds 40 degrees!");
        }
    }
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    private static void connect(MqttClient client, MqttConnectOptions options) throws MqttException {
        if (!client.isConnected()) {
            client.connect(options);
        }
    }
    private void disconnect() throws MqttException {
        client.disconnect();
    }
}
