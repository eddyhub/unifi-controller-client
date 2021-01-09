package de.jsyn.unifi.controller.tools;

import net.glxn.qrgen.core.scheme.Wifi;
import net.glxn.qrgen.javase.QRCode;
import org.passay.CharacterData;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;

import java.io.File;

public class PasswordTool {

    private static final PasswordGenerator PASSWORD_GENERATOR = new PasswordGenerator();
    private static final CharacterRule ALPHA_RULE = new CharacterRule(EnglishCharacterData.Alphabetical);
    private static final CharacterRule DIGIT_RULE = new CharacterRule(EnglishCharacterData.Digit);
    private static final CharacterRule SPECI_RULE = new CharacterRule(new CharacterData() {
        @Override
        public String getErrorCode() {
            return "INSUFFICIENT_SPECIAL";
        }

        @Override
        public String getCharacters() {
            return "!#$%&*+,-.;<=>?@_";
        }
    });

    public static String generatePassword(int size) {
        return PASSWORD_GENERATOR.generatePassword(size, ALPHA_RULE, DIGIT_RULE, SPECI_RULE);
    }

    private static QRCode generateQrCode(String ssid, String password) {
        Wifi wifi = new Wifi();
        wifi.setAuthentication(Wifi.Authentication.WPA);
        wifi.setPsk(password);
        wifi.setSsid(ssid);
        return QRCode.from(wifi).withSize(640, 480);
    }

    public static File generateQrCodeFile(String ssid, String password) {
        return generateQrCode(ssid, password).file();
    }

    public static byte[] generateQrCodeBytes(String ssid, String password) {
        return generateQrCode(ssid, password).stream().toByteArray();
    }
}
