package org.example;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import javax.net.ssl.SSLSocketFactory;
import java.util.Arrays;
import static java.nio.charset.StandardCharsets.UTF_8;

public class Sensor {
    private final MqttClient client;
    public Sensor() throws MqttException {
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
                // Called when the client lost the connection to the broker
                System.out.println("Connection lost. Reconnecting...");
                try {
                    connect(client, mqttConnectOptions);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void messageArrived(String topic, MqttMessage message) {
                System.out.println(topic + ": " + Arrays.toString(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                // Called when an outgoing publish is complete
            }
        });

        client.subscribe("#", 1);
    }
    public void publishRandomTemperature() throws MqttException, InterruptedException {
        while (true) {
            String temperatureTopic = "Temp";
            // Simulate a sinusoidal pattern for temperature fluctuations
            double baseTemperature = 25.0;
            double amplitude = 10.0;
            double frequency = 0.1;

            double randomTemperature = baseTemperature + amplitude * Math.sin(frequency * System.currentTimeMillis() / 1000.0);
            randomTemperature = Math.round(randomTemperature * 100.0) / 100.0;

            String payload = String.format("%.2f", randomTemperature);
            client.publish(temperatureTopic, payload.getBytes(UTF_8), 1, false);
            System.out.println("Published temperature: " + payload);
            // Wait for 5 seconds before publishing the next temperature value
            Thread.sleep(5000);
        }
    }
    private static void connect(MqttClient client, MqttConnectOptions options) throws MqttException {
        if (!client.isConnected()) {
            client.connect(options);
        }
    }
    public void disconnect() throws MqttException {
        client.disconnect();
    }
}
