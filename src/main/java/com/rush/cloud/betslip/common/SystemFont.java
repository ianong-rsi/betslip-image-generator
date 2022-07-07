package com.rush.cloud.betslip.common;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.util.Optional;

public class SystemFont {

    public static final Font DM_SANS_BOLD = loadFont("/fonts/DMSans-Bold.ttf");
    public static final Font DM_SANS_REGULAR = loadFont("/fonts/DMSans-Regular.ttf");
    public static final Font DM_SANS_MEDIUM = loadFont("/fonts/DMSans-Medium.ttf");
    public static final Font TEKO_BOLD = loadFont("/fonts/Teko-Bold.ttf");
    public static final Font TEKO_SEMI_BOLD = loadFont("/fonts/Teko-SemiBold.ttf");

    private static Font loadFont(String fontResource) {
        return Optional.ofNullable(SystemFont.class.getResourceAsStream(fontResource))
                .map(inputStream -> {
                    try {
                        return Font.createFont(Font.TRUETYPE_FONT, inputStream);
                    } catch (FontFormatException | IOException e) {
                        return null;
                    }
                })
                .orElseThrow(() -> new RuntimeException("unable to load font: " + fontResource));
    }
}
