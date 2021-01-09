package de.jsyn.unifi.controller.tools.messaging;

import org.eclipse.paho.client.mqttv3.*;

import java.io.IOException;

public class Mqtt implements AutoCloseable {

    private IMqttClient publisher;

    public Mqtt(String serverUri, String clientId) throws MqttException {
        publisher = new MqttClient(serverUri, clientId);
        publisher.connect();
    }

    public void sendMessage(String topic, String msg) throws MqttException {
        MqttMessage message = new MqttMessage(msg.getBytes());
        publisher.publish(topic, message);
    }

    public void sendImage(String topic, byte[] image) throws MqttException {
        MqttMessage message = new MqttMessage(image);
        publisher.publish(topic, message);
    }

    public void disconnect() throws MqttException {
        publisher.disconnect();
    }

    @Override
    public void close() throws IOException {
        try {
            disconnect();
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }
}
