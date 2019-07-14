package de.jsyn.unifi.controller.tools;

import java.io.File;

public interface MessagingTool {
    void sendMessage(String subject, String msg, File qrcode);
}
