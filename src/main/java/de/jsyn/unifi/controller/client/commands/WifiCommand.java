package de.jsyn.unifi.controller.client.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandDescription = "List/Edit wifi settings")
public class WifiCommand {

    public static final String COMMAND_NAME = "wifi";

    @Parameter(names = { "list", "-l"}, description = "List all available wifis.")
    public Boolean listWifis = false;

    @Parameter(names = {"--wifi", "--ssid"}, description = "Name/SSID of the wifi.")
    public String wifiName;

    @Parameter(names = {"--wifiPassword"}, description = "Password to set for the specified wifi.")
    public String wifiPassword;
}
