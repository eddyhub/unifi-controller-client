package de.jsyn.unifi.controller.tools;

import org.passay.CharacterData;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;

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
}
