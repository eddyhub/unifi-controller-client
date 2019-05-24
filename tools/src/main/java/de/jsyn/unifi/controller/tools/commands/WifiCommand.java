package de.jsyn.unifi.controller.tools.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandDescription = "List/Edit wifi settings")
public class WifiCommand {

    public static final String COMMAND_NAME = "wifi";

    @Parameter(names = { "--list", "-l"}, description = "List all available wifis.")
    public Boolean listWifis = false;

    @Parameter(names = {"--wifi", "--ssid"}, description = "Name/SSID of the wifi.")
    public String wifiName;

    @Parameter(names = {"--wifiPassword"}, description = "Password to set for the specified wifi.")
    public String wifiPassword;

    @Parameter(names = {"--generateWifiPassword"}, description = "Generate random password.")
    public Boolean generateWifiPassword = false;

    @Parameter(names = {"--sendViaMail"}, description = "Send set password via mail.")
    public Boolean sendViaMail = false;
}
