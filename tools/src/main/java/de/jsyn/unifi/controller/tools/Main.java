package de.jsyn.unifi.controller.tools;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import de.jsyn.unifi.controller.client.ApiException;
import de.jsyn.unifi.controller.client.model.Login;
import de.jsyn.unifi.controller.tools.commands.WifiCommand;
import de.jsyn.unifi.controller.tools.messaging.Mail;
import de.jsyn.unifi.controller.tools.messaging.Mqtt;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.IOException;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Logger;

public class Main {

    static final String DEFAULT_SITE = "default";
    static final String REST_ENDPOINT = "api";
    static final String PROGRAMM_NAME = "unifi-tools";
    static final String CMD_MAIL_USERNAME = "--mail-username";
    static final String CMD_MAIL_PASSWORD = "--mail-password";
    static final String CMD_MAIL_HOST = "--mail-host";
    static final String CMD_MQTT_USERNAME = "--mqtt-username";
    static final String CMD_MQTT_PASSWORD = "--mqtt-password";
    static final String CMD_MQTT_HOST_URI = "--mqtt-host-uri";
    static final String CMD_MQTT_JSON_TOPIC = "--mqtt-json-topic";
    static final String CMD_MQTT_IMAGE_TOPIC = "--mqtt-image-topic";
    private static final Logger LOG = Logger.getLogger(Main.class.getName());
    @Parameter(names = {"--controller", "-c"}, description = "Hostname of the controller.", required = true, order = 0)
    private String host;

    @Parameter(names = {"--username", "-u"}, description = "Username to login to controller.", required = true, order = 10)
    private String username;

    @Parameter(names = {"--password", "-p"}, description = "Password to login to controller.", required = true, order = 20)
    private String password;

    @Parameter(names = {"--insecure"}, description = "Don't check the server certificate.", order = 40)
    private boolean insecure = false;

    @Parameter(names = {"--site", "-s"}, description = "Name of the site to login.", order = 30)
    private String site = DEFAULT_SITE;

    @Parameter(names = CMD_MAIL_USERNAME, description = "Username to login to SMTP host.", order = 40)
    private String mailUsername;

    @Parameter(names = CMD_MAIL_PASSWORD, description = "Password to login to SMTP host.", order = 40)
    private String mailPassword;

    @Parameter(names = CMD_MAIL_HOST, description = "SMTP host.", order = 40)
    private String mailHost;

    @Parameter(names = {"--mail-from"}, description = "From field of email.", order = 40)
    private String mailFrom;

    @Parameter(names = {"--mail-to"}, description = "To field of email.", order = 40)
    private String mailTo;

    @Parameter(names = {"--mail-subject"}, description = "Subject for the mail..", order = 40)
    private String mailSubject;

    @Parameter(names = {"--mail-message"}, description = "Message for the mail.. (password will be attached)", order = 40)
    private String mailMessage = "";

    @Parameter(names = CMD_MQTT_USERNAME, description = "Username to login to MQTT host.", order = 40)
    private String mqttUsername;

    @Parameter(names = CMD_MQTT_PASSWORD, description = "Password to login to MQTT host.", order = 40)
    private String mqttPassword;

    @Parameter(names = CMD_MQTT_HOST_URI, description = "Uri to MQTT host.", order = 40)
    private String mqttHostUri;

    @Parameter(names = CMD_MQTT_JSON_TOPIC, description = "MQTT json topic to send to.", order = 40)
    private String mqttJsonTopic = "";

    @Parameter(names = CMD_MQTT_IMAGE_TOPIC, description = "MQTT image topic to send to.", order = 40)
    private String mqttImageTopic = "";

    @Parameter(names = {"--mqtt-client-id"}, description = "MQTT client id", order = 40)
    private String clientId = "";

    @Parameter(names = {"--help", "-h"}, help = true, order = 999999)
    private boolean help = false;

    public static void main(String[] args) throws ApiException {
        Main mainCmd = new Main();
        WifiCommand wifiCmd = new WifiCommand();
        JCommander jc = JCommander.newBuilder()
                .addObject(mainCmd)
                .addCommand(WifiCommand.COMMAND_NAME, wifiCmd)
                .programName(PROGRAMM_NAME)
                .build();
        try {
            jc.parse(args);
        } catch (ParameterException e) {
            System.out.println(e.getMessage() + "\n\n");
            mainCmd.help = true;
        }

        if (mainCmd.help) {
            jc.usage();
            System.exit(-1);
        }

        Login login = new Login();
        login.setUsername(mainCmd.username);
        login.setPassword(mainCmd.password);

        if (!mainCmd.host.endsWith(REST_ENDPOINT)) {
            mainCmd.host = mainCmd.host + "/" + REST_ENDPOINT;
        }

        switch (jc.getParsedCommand()) {
            case WifiCommand.COMMAND_NAME:
                String message = null;
                String jsonMessage = null;
                String generatedPassword = null;
                WifiTool wt = new WifiTool(mainCmd.host, login, mainCmd.insecure);
                if (wifiCmd.listWifis) {
                    wt.getWifiList(mainCmd.site).forEach(System.out::println);
                } else if (wifiCmd.wifiName != null && wifiCmd.wifiPassword != null && !wifiCmd.wifiName.isEmpty() && !wifiCmd.wifiPassword.isEmpty()) {
                    wt.updatePassword(mainCmd.site, wifiCmd.wifiName, wifiCmd.wifiPassword);
                } else if (wifiCmd.generateWifiPassword && (wifiCmd.sendViaMail || wifiCmd.sendViaMqtt)) {
                    generatedPassword = PasswordTool.generatePassword(16);
                    message = String.format("%s\nSSID/WLAN-Name: %s\nPasswort: %s",
                            mainCmd.mailMessage, wifiCmd.wifiName, generatedPassword);
                    jsonMessage = String.format("{ \"ssid\":\"%s\",\"password\":\"%s\"}", wifiCmd.wifiName, generatedPassword);
                    wt.updatePassword(mainCmd.site, wifiCmd.wifiName, generatedPassword);

                }
                if (wifiCmd.sendViaMail) {
                    if (mainCmd.mailHost != null && mainCmd.mailUsername != null && mainCmd.mailPassword != null) {
                        Properties mailProperties = new Mail.MailPropertiesBuilder()
                                .setHost(mainCmd.mailHost)
                                .setUsername(mainCmd.mailUsername)
                                .setPassword(mainCmd.mailPassword)
                                .setFrom(mainCmd.mailFrom)
                                .setTo(mainCmd.mailTo)
                                .build();
                        LOG.info(message);
                        Mail mailMessage = new Mail(mailProperties);
                        mailMessage.sendMessage(mainCmd.mailSubject, message, PasswordTool.generateQrCodeFile(wifiCmd.wifiName, generatedPassword));
                    } else {
                        System.out.println(String.format("Please specify at least the options %s, %s and %s.", CMD_MAIL_HOST, CMD_MAIL_USERNAME, CMD_MAIL_PASSWORD));
                        jc.usage();
                        System.exit(-1);
                    }
                }
                if (wifiCmd.sendViaMqtt) {
                    if (mainCmd.mqttHostUri != null && mainCmd.mqttUsername != null && mainCmd.mqttPassword != null && (!mainCmd.mqttJsonTopic.isEmpty() || !mainCmd.mqttImageTopic.isEmpty())) {
                        LOG.info(jsonMessage);
                        if (mainCmd.clientId.isEmpty()) {
                            mainCmd.clientId = UUID.randomUUID().toString();
                        }
                        try (Mqtt mqtt = new Mqtt(mainCmd.mqttHostUri, mainCmd.clientId)) {
                            if (!mainCmd.mqttJsonTopic.isEmpty())
                                mqtt.sendMessage(mainCmd.mqttJsonTopic, jsonMessage);
                            if (!mainCmd.mqttImageTopic.isEmpty())
                                mqtt.sendImage(mainCmd.mqttImageTopic, PasswordTool.generateQrCodeBytes(wifiCmd.wifiName, generatedPassword));
                        } catch (IOException | MqttException e) {
                            throw new RuntimeException("Unable to connect to mqtt broker.", e);
                        }
                    } else {
                        System.out.println(String.format("Please specify at least the options %s, %s and %s.", CMD_MQTT_HOST_URI, CMD_MQTT_USERNAME, CMD_MQTT_PASSWORD));
                        jc.usage();
                        System.exit(-1);
                    }
                }
        }
    }
}
