package org.example;

import javafx.application.Application;
import org.eclipse.paho.client.mqttv3.MqttException;

public class MainApplication {
    public static void main(String[] args) {
        // Start the publisher (Sensor)
        try {
            Sensor sensor = new Sensor();
            Thread sensorThread = new Thread(() -> {
                try {
                    sensor.publishRandomTemperature();
                } catch (MqttException | InterruptedException e) {
                    e.printStackTrace();
                }
            });
            sensorThread.start();
        } catch (MqttException e) {
            e.printStackTrace();
        }
        // Start the subscriber with GUI
        Application.launch(TemperatureSubscriberGUI.class, args);
    }
}
