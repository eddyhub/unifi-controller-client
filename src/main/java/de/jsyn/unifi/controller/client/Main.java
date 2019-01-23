package de.jsyn.unifi.controller.client;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import de.jsyn.unifi.controller.client.commands.WifiCommand;
import de.jsyn.unifi.controller.client.model.Login;

public class Main {

    private static String DEFAULT_SITE = "default";
    private static String REST_ENDPOINT = "api";
    private static String PROGRAMM_NAME = "unifi-client";

    @Parameter(names = {"--controller", "-c"}, description = "Hostname of the controller.", required = true, order = 0)
    private String host;

    @Parameter(names = {"--username", "-u"}, description = "Username to login to controller.", required = true, order = 10)
    private String username;

    @Parameter(names = {"--password", "-p"}, description = "Password to login to controller.", required = true, order = 20)
    private String password;

    @Parameter(names = {"--site", "-s"}, description = "Name of the site to login.", order = 30)
    private String site = DEFAULT_SITE;

    @Parameter(names = {"--help", "-h"}, help = true, order = 999999)
    private boolean help = false;

    public static void main(String[] args) throws ApiException {
        Main mainCmd = new Main();
        WifiCommand wifiCmd = new WifiCommand();
        JCommander jc = new JCommander().newBuilder()
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
            mainCmd.host = mainCmd.host + "/" +  REST_ENDPOINT;
        }

        switch (jc.getParsedCommand()) {
            case WifiCommand.COMMAND_NAME:
                WifiTool wt = new WifiTool(mainCmd.host, login);
                if (wifiCmd.listWifis) {
                    wt.getWifiList(mainCmd.site).stream().forEach(System.out::println);
                }
                if (!wifiCmd.wifiName.isEmpty() && !wifiCmd.wifiPassword.isEmpty()) {
                    wt.updatePassword(mainCmd.site, wifiCmd.wifiName, wifiCmd.wifiPassword);
                }
        }
    }
}
