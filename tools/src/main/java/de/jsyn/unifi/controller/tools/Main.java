package de.jsyn.unifi.controller.tools;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import de.jsyn.unifi.controller.client.ApiException;
import de.jsyn.unifi.controller.client.model.Login;
import de.jsyn.unifi.controller.tools.commands.WifiCommand;

import javax.mail.MessagingException;
import java.time.LocalDateTime;
import java.util.logging.Logger;

public class Main {

    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    static final String DEFAULT_SITE = "default";
    static final String REST_ENDPOINT = "api";
    static final String PROGRAMM_NAME = "unifi-tools";
    static final String CMD_MAIL_USERNAME = "--mail-username";
    static final String CMD_MAIL_PASSWORD = "--mail-password";
    static final String CMD_MAIL_HOST = "--mail-host";


    @Parameter(names = {"--controller", "-c"}, description = "Hostname of the controller.", required = true, order = 0)
    private String host;

    @Parameter(names = {"--username", "-u"}, description = "Username to login to controller.", required = true, order = 10)
    private String username;

    @Parameter(names = {"--password", "-p"}, description = "Password to login to controller.", required = true, order = 20)
    private String password;

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

    @Parameter(names = {"--help", "-h"}, help = true, order = 999999)
    private boolean help = false;

    public static void main(String[] args) throws ApiException, MessagingException {
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
                String generatedPassword = null;
                WifiTool wt = new WifiTool(mainCmd.host, login);
                if (wifiCmd.listWifis) {
                    wt.getWifiList(mainCmd.site).forEach(System.out::println);
                } else if (wifiCmd.wifiName != null && wifiCmd.wifiPassword != null && !wifiCmd.wifiName.isEmpty() && !wifiCmd.wifiPassword.isEmpty()) {
                    wt.updatePassword(mainCmd.site, wifiCmd.wifiName, wifiCmd.wifiPassword);
                } else if (wifiCmd.generateWifiPassword && wifiCmd.sendViaMail) {
                    generatedPassword = PasswordTool.generatePassword(16);
                    message = String.format("Gueltig ab: %s\nGueltig bis: %s\nSSID/WLAN-Name: %s\nPasswort: %s",
                            LocalDateTime.now(), LocalDateTime.now().plusHours(24), wifiCmd.wifiName, generatedPassword);
                    wt.updatePassword(mainCmd.site, wifiCmd.wifiName, generatedPassword);

                }
                if (wifiCmd.sendViaMail) {
                    if (mainCmd.mailHost != null && mainCmd.mailUsername != null && mainCmd.mailPassword != null) {
                        final MailTool mailTool = new MailTool.MailToolBuilder()
                                .setHost(mainCmd.mailHost)
                                .setUsername(mainCmd.mailUsername)
                                .setPassword(mainCmd.mailPassword)
                                .build();
                        LOG.info(message);
                        mailTool.sendMessage(mainCmd.mailFrom, mainCmd.mailTo, mainCmd.mailSubject, message, PasswordTool.generateQrCode(wifiCmd.wifiName, generatedPassword));
                    } else {
                        System.out.println(String.format("Please specify at least the options %s, %s and %s.", CMD_MAIL_HOST, CMD_MAIL_USERNAME, CMD_MAIL_PASSWORD));
                        jc.usage();
                        System.exit(-1);
                    }
                }
        }
    }
}